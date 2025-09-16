# AOP
## 1.AOP的不同实现方式

AOP有两种实现思路，编译时织入增强逻辑和运行时织入增强逻辑
- 编译时织入增强逻辑：AspectJ采用，编译时直接修改字节码
- 运行时织入增强逻辑：AOP采用，运行时生成动态代理（但spring也集成了AspectJ）

## 2.Spring AOP和Bean生命周期的关联
```
1. Bean定义解析
   ↓
2. Bean实例化前处理 (postProcessBeforeInstantiation)
   ├─ 利用已解析好的Advisor集合，检查当前Bean是否符合切面（PointCut）规则
   └─ 此阶段的核心输出是一个决定：这个Bean需要被代理吗？ 这个决定的结果会被缓存下来，供后续阶段使用
   ↓
3. Bean实例化 (createBeanInstance)
   ↓
4. 属性填充 (populateBean)
   ↓
5. Bean初始化处理
   ↓
6. Bean初始化后处理 (postProcessAfterInitialization)
   └─ AOP利用实例化前是否需要创建代理的判断，在此阶段创建代理对象并放入容器中
   ↓
7. Bean就绪，注册到容器
```

### 2.1 AOP生成代理的方式
#### 2.1.1 生成代理的选择方式
- JDK动态代理 ：当目标对象实现了接口时使用
- CGLIB代理 ：当目标对象没有实现接口或强制使用类代理时使用，通过生成目标类的子类来生成代理对象
```
DefaultAopProxyFactory类内部逻辑

// 代理选择逻辑
if (!NativeDetector.inNativeImage() &&
    (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config))) {
    // 使用CGLIB代理的条件：
    // 1. optimize标志为true
    // 2. proxyTargetClass标志为true  
    // 3. 没有指定代理接口
    if (targetClass.isInterface() || Proxy.isProxyClass(targetClass) || ClassUtils.isLambdaClass(targetClass)) {
        return new JdkDynamicAopProxy(config);
    }
    return new ObjenesisCglibAopProxy(config);
} else {
    return new JdkDynamicAopProxy(config);
}
```


### 2.2 AOP关键概念
3个关键要素：切点、通知、连接点（切面包含切点和通知）

#### 连接点：程序执行的一个点，如方法调用

#### 通知（增强）：在原有代码之外增强的逻辑
- 前置增强（Before Advice）：在目标方法执行前进行处理；
- 后置增强（After Advice）：在目标方法执行后执行，无论成功与否；
- 返回后增强（After Returning Advice）：在目标方法成功执行后处理返回值；
- 异常后增强（After Throwing Advice）：在目标方法抛出异常时执行处理；
- 环绕增强（Around Advice）：可控制目标方法的执行，前后均可处理

#### 切点：定义了在哪些连接点上应用通知的表达式
- 方法执行表达式（execution）：匹配方法的执行，可以指定类、方法名、参数等。
  - 示例：execution(* com.example.service.*.*(..))

  - 注解表达式（@annotation）：匹配被特定注解标注的方法。
  - 示例：@annotation(org.springframework.web.bind.annotation.PostMapping)

- 类类型表达式（within）：匹配特定类型的类或其子类中的方法。
 - 示例：within(com.example.service.*)

- bean 表达式（bean）：匹配 Spring 容器中指定名称的 bean。
 - 示例：bean(myService)

- 切入点表达式（@pointcut）：定义切入点，可以组合多种表达式，便于复用。
 - 示例：@Pointcut("execution(* com.example.service.*.*(..))")

### 2.3 常见问题
- (1)cglib无法增强final类和final方法；jdk动态代理无法增强final方法


- (2)cglib可增强public、protected、包可见(缺省)方法；jdk动态代理仅增强public方法


- (3)当在被代理类内部使用this调用另一个被增强的方法时，另一个方法的增强逻辑不会生效；不能增强构造函数。 
  this则仍然使用的是原来的类对象；aop代理类是在初始化后被创建，不能增强实例化、属性赋值、初始化时的逻辑

- (4)增强类也需要正确的依赖注入


- (5)在增强类中使用非线程安全的变量，会导致并发问题
