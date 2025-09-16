# 机制示例说明
## 1.三级缓存结构
```
// 一级缓存：存放完整的单例Bean（成品）
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
// 二级缓存：存放早期的Bean引用（半成品），用于解决循环依赖
private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);
// 三级缓存：存放Bean的ObjectFactory（工厂），用于处理AOP代理等特殊情况
private final Map<String, ObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>(16);
```

## 2.三级缓存使用说明
```
A依赖B，B依赖A

1.A经过实例化之后，放入第三层缓存；
2.A属性注入阶段注入B时，依次查找一、二、三级缓存，没有B；
3.会创建B，B经过实例化后放入第三级缓存，进行属性注入A，依次查找一、二、三级缓存，在第三级缓存找到A
4.将A创建之后的原始对象从三级缓存移到二级缓存，并完成属性注入
5.A属性注入阶段注入B
6.A完成属性注入和AOP代理的创建代理对象后，发现第二级缓存已经有一个原始对象，则会替换原始对象
```

创建机制代码流程
```
// 假设ServiceA和ServiceB循环依赖，且ServiceA需要AOP代理

// 1. 创建ServiceA
ServiceA serviceA = new ServiceA();
addSingletonFactory("serviceA", () -> createProxy(serviceA));

// 2. 初始化ServiceA，需要注入ServiceB
// 3. 创建ServiceB
ServiceB serviceB = new ServiceB();

// 4. 初始化ServiceB，需要注入ServiceA
Object serviceAProxy = getSingleton("serviceA", false);  // 触发代理创建
// 此时：earlySingletonObjects.put("serviceA", serviceAProxy)

// 5. ServiceB初始化完成，注入的是serviceAProxy
singletonObjects.put("serviceB", serviceB);

// 6. 回到ServiceA的初始化
Object exposedObject = serviceA;  // 原始对象
Object earlySingletonReference = getSingleton("serviceA", false);  // 代理对象

if (exposedObject == serviceA) {  // true
    exposedObject = earlySingletonReference;  // 用代理对象替换！
}

// 7. 最终结果
singletonObjects.put("serviceA", exposedObject);  // 存储的是代理对象
```

### 不能使用二级缓存的原因
- 如果只有二级缓存，只能区分两种状态，一级缓存用来存放完整的Bean，二级存放不完整的Bean
- 上述流程就变成
```
A依赖B，B依赖A

1.A经过实例化之后，放入第二层缓存；
2.A属性注入阶段注入B时，依次查找一、二级缓存，没有B；
3.会创建B，B经过实例化后放入第二级缓存，进行属性注入A，依次查找一、二级缓存，在第二级缓存找到A
4.B基于A的原始对象完成属性注入
5.A属性注入阶段注入B
```
- A有AOP代理时就会出现问题
```
B从二级缓存中获取到的是没有被代理的A，后续A被完整创建后放入一级缓存，则会出现B依赖的A和实际的A不符
```
- 三级缓存