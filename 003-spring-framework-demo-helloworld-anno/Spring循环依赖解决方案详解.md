# Spring循环依赖解决方案详解

## 1. Bean的完整生命周期

### 1.1 Bean生命周期的核心阶段

Spring Bean的生命周期可以分为以下几个核心阶段：

```
Bean定义注册 → 实例化 → 属性注入 → 初始化 → 使用 → 销毁
```

### 1.2 详细生命周期流程

#### 阶段1：Bean定义注册
```java
// 1. 扫描带有Spring注解的类
@ComponentScan("tech.pdai.springframework")

// 2. 解析注解，生成BeanDefinition
BeanDefinition beanDefinition = new BeanDefinition();
beanDefinition.setBeanClass(UserService.class);
beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

// 3. 注册到BeanDefinitionRegistry
beanDefinitionRegistry.registerBeanDefinition("userService", beanDefinition);
```

#### 阶段2：Bean实例化 (Instantiation)
```java
// 1. 调用构造方法创建Bean实例
UserService userService = new UserService();  // 此时属性都是默认值

// 2. 将Bean的工厂对象放入三级缓存（解决循环依赖的关键）
if (isSingleton(beanName)) {
    singletonFactories.put(beanName, () -> getEarlyBeanReference(beanName, bean));
}
```

#### 阶段3：属性注入 (Population)
```java
// 1. 基本属性注入
userService.setName("default");

// 2. 依赖属性注入（这里可能触发循环依赖）
for (PropertyValue pv : beanDefinition.getPropertyValues()) {
    if (pv.getValue() instanceof BeanReference) {
        // 需要注入其他Bean
        BeanReference ref = (BeanReference) pv.getValue();
        Object dependency = getBean(ref.getBeanName());  // 可能触发循环依赖
        // 通过反射设置属性值
        ReflectionUtils.setField(field, userService, dependency);
    }
}
```

#### 阶段4：Bean初始化 (Initialization)
```java
// 1. Aware接口回调
if (bean instanceof BeanNameAware) {
    ((BeanNameAware) bean).setBeanName(beanName);
}
if (bean instanceof ApplicationContextAware) {
    ((ApplicationContextAware) bean).setApplicationContext(applicationContext);
}

// 2. BeanPostProcessor前置处理
for (BeanPostProcessor processor : beanPostProcessors) {
    bean = processor.postProcessBeforeInitialization(bean, beanName);
}

// 3. @PostConstruct方法执行
if (hasPostConstruct) {
    Method postConstruct = findPostConstructMethod(bean.getClass());
    postConstruct.invoke(bean);
}

// 4. InitializingBean.afterPropertiesSet()
if (bean instanceof InitializingBean) {
    ((InitializingBean) bean).afterPropertiesSet();
}

// 5. 自定义init方法
if (hasCustomInitMethod) {
    Method initMethod = findInitMethod(beanDefinition);
    initMethod.invoke(bean);
}

// 6. BeanPostProcessor后置处理
for (BeanPostProcessor processor : beanPostProcessors) {
    bean = processor.postProcessAfterInitialization(bean, beanName);
}
```

#### 阶段5：Bean使用
```java
// Bean完全初始化后，可以正常使用
UserService userService = context.getBean(UserService.class);
userService.createUser();
```

#### 阶段6：Bean销毁
```java
// 1. @PreDestroy方法执行
if (hasPreDestroy) {
    Method preDestroy = findPreDestroyMethod(bean.getClass());
    preDestroy.invoke(bean);
}

// 2. DisposableBean.destroy()
if (bean instanceof DisposableBean) {
    ((DisposableBean) bean).destroy();
}

// 3. 自定义destroy方法
if (hasCustomDestroyMethod) {
    Method destroyMethod = findDestroyMethod(beanDefinition);
    destroyMethod.invoke(bean);
}
```

### 1.3 生命周期中的扩展点

```java
// 1. Aware接口
public interface BeanNameAware {
    void setBeanName(String name);
}

public interface ApplicationContextAware {
    void setApplicationContext(ApplicationContext applicationContext);
}

// 2. BeanPostProcessor
public interface BeanPostProcessor {
    Object postProcessBeforeInitialization(Object bean, String beanName);
    Object postProcessAfterInitialization(Object bean, String beanName);
}

// 3. 生命周期接口
public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}

public interface DisposableBean {
    void destroy() throws Exception;
}

// 4. 注解回调
@PostConstruct
public void init() {
    // 初始化逻辑
}

@PreDestroy
public void cleanup() {
    // 清理逻辑
}
```

## 2. 什么是循环依赖？

循环依赖是指两个或多个Bean之间相互依赖，形成一个闭环的情况。

### 2.1 循环依赖的典型场景

```java
// 场景1：构造器循环依赖
@Service
public class UserService {
    private final OrderService orderService;
    
    public UserService(OrderService orderService) {  // 构造器注入
        this.orderService = orderService;
    }
}

@Service
public class OrderService {
    private final UserService userService;
    
    public OrderService(UserService userService) {  // 构造器注入
        this.userService = userService;
    }
}

// 场景2：字段循环依赖
@Service
public class UserService {
    @Autowired
    private OrderService orderService;  // 字段注入
}

@Service
public class OrderService {
    @Autowired
    private UserService userService;  // 字段注入
}
```

## 3. Spring解决循环依赖的机制

### 3.1 三级缓存机制

Spring使用三级缓存来解决循环依赖问题：

```java
// Spring容器中的三级缓存
public class DefaultSingletonBeanRegistry {
    
    // 一级缓存：完全初始化好的Bean
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
    
    // 二级缓存：早期暴露的Bean（未完全初始化）
    private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);
    
    // 三级缓存：Bean的工厂对象
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);
}
```

### 3.2 循环依赖在生命周期中的解决时机

#### 关键时机：属性注入阶段

```java
// 在populateBean阶段，当发现循环依赖时
protected void populateBean(String beanName, RootBeanDefinition mbd, BeanWrapper bw) {
    // 1. 获取所有需要注入的属性
    PropertyValues pvs = mbd.getPropertyValues();
    
    // 2. 遍历属性，注入依赖
    for (PropertyValue pv : pvs) {
        if (pv.getValue() instanceof BeanReference) {
            BeanReference ref = (BeanReference) pv.getValue();
            String refName = ref.getBeanName();
            
            // 3. 获取依赖Bean（这里可能触发循环依赖）
            Object refBean = getBean(refName);
            
            // 4. 通过反射设置属性值
            setPropertyValue(bean, pv, refBean);
        }
    }
}
```

#### 循环依赖检测和解决

```java
public Object getBean(String beanName) {
    // 1. 检查是否正在创建中（检测循环依赖）
    if (isCurrentlyInCreation(beanName)) {
        // 发现循环依赖，尝试从三级缓存获取早期引用
        Object earlyReference = getEarlyBeanReference(beanName);
        if (earlyReference != null) {
            return earlyReference;
        }
    }
    
    // 2. 从一级缓存获取
    Object bean = singletonObjects.get(beanName);
    if (bean != null) {
        return bean;
    }
    
    // 3. 从二级缓存获取早期引用
    bean = earlySingletonObjects.get(beanName);
    if (bean != null) {
        return bean;
    }
    
    // 4. 从三级缓存获取工厂对象
    ObjectFactory<?> factory = singletonFactories.get(beanName);
    if (factory != null) {
        // 调用工厂方法获取早期引用
        bean = factory.getObject();
        // 放入二级缓存
        earlySingletonObjects.put(beanName, bean);
        // 从三级缓存移除
        singletonFactories.remove(beanName);
        return bean;
    }
    
    // 5. 创建Bean
    return createBean(beanName);
}
```

### 3.3 解决流程详解

#### 步骤1：创建UserService Bean
```java
// 1. 创建UserService实例（此时orderService属性为null）
UserService userService = new UserService();  // 构造方法执行

// 2. 将UserService的工厂对象放入三级缓存
singletonFactories.put("userService", () -> {
    // 这个工厂对象可以返回UserService的早期引用
    return getEarlyBeanReference("userService", userService);
});
```

#### 步骤2：属性注入时发现循环依赖
```java
// 3. 开始注入UserService的属性
// 发现需要注入OrderService
OrderService orderService = getBean("orderService");

// 4. 创建OrderService实例
OrderService orderService = new OrderService();

// 5. 将OrderService的工厂对象放入三级缓存
singletonFactories.put("orderService", () -> {
    return getEarlyBeanReference("orderService", orderService);
});
```

#### 步骤3：OrderService需要注入UserService
```java
// 6. 开始注入OrderService的属性
// 发现需要注入UserService
UserService userService = getBean("userService");

// 7. 从三级缓存中获取UserService的早期引用
ObjectFactory<?> factory = singletonFactories.get("userService");
if (factory != null) {
    // 调用工厂方法，获取早期引用
    Object earlyReference = factory.getObject();
    
    // 将早期引用放入二级缓存
    earlySingletonObjects.put("userService", earlyReference);
    
    // 从三级缓存中移除
    singletonFactories.remove("userService");
    
    return earlyReference;
}
```

#### 步骤4：完成循环依赖解决
```java
// 8. OrderService获得UserService的早期引用，完成属性注入
orderService.setUserService(userService);  // 注入早期引用

// 9. OrderService完成初始化，放入一级缓存
singletonObjects.put("orderService", orderService);

// 10. UserService完成属性注入和初始化，放入一级缓存
singletonObjects.put("userService", userService);
```

## 4. 循环依赖的限制条件

### 4.1 支持的情况

1. **单例Bean** - 只有单例Bean才能解决循环依赖
2. **字段注入** - `@Autowired`、`@Resource`等字段注入
3. **setter注入** - setter方法注入

### 4.2 不支持的情况

1. **构造器注入** - 构造器循环依赖无法解决
2. **原型Bean** - 原型Bean的循环依赖无法解决
3. **@Async方法** - 异步方法的循环依赖可能有问题

### 4.3 构造器循环依赖示例

```java
@Service
public class UserService {
    private final OrderService orderService;
    
    public UserService(OrderService orderService) {  // 构造器注入
        this.orderService = orderService;
    }
}

@Service
public class OrderService {
    private final UserService userService;
    
    public OrderService(UserService userService) {  // 构造器注入
        this.userService = userService;
    }
}

// 启动时会抛出异常：
// BeanCurrentlyInCreationException: Error creating bean with name 'userService': 
// Requested bean is currently in creation: Is there an unresolvable circular reference?
```

### 4.4 为什么构造器循环依赖无法解决？

**关键原因：时机问题**

```java
// 构造器注入发生在实例化阶段，此时Bean还没有被创建
public Object createBean(String beanName) {
    // 1. 实例化阶段：调用构造器
    Object bean = instantiateBean(beanName);  // 这里需要注入依赖，但依赖还未创建
    
    // 2. 此时无法放入三级缓存，因为Bean实例还没有创建完成
    // 3. 形成死锁：两个Bean都在等待对方先创建完成
}
```

**详细分析：**
1. **构造器调用时机**：在`instantiateBean()`阶段
2. **依赖注入时机**：构造器参数需要立即注入
3. **三级缓存时机**：Bean实例化完成后才能放入
4. **结果**：无法使用三级缓存机制解决循环依赖

### 4.5 为什么原型Bean循环依赖无法解决？

**关键原因：不经过缓存**

```java
// 原型Bean每次都是新实例，不经过缓存管理
if (isPrototype(beanName)) {
    Object bean = createBean(beanName);
    return bean;  // 直接返回，不放入任何缓存
}

// 三级缓存机制只对单例Bean有效
if (isSingleton(beanName)) {
    // 只有单例Bean才会使用三级缓存
    singletonFactories.put(beanName, factory);
}
```

## 5. 实际业务中的循环依赖处理

### 5.1 设计层面避免循环依赖

```java
// 方案1：重新设计架构，提取公共接口
public interface UserOperations {
    void createUser();
    void getUserInfo();
}

public interface OrderOperations {
    void createOrder();
    void getOrderInfo();
}

@Service
public class UserService implements UserOperations {
    @Autowired
    private OrderOperations orderOperations;  // 依赖接口，不依赖具体实现
}

@Service
public class OrderService implements OrderOperations {
    @Autowired
    private UserOperations userOperations;  // 依赖接口，不依赖具体实现
}

// 方案2：使用事件机制解耦
@Service
public class UserService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    public void createUser() {
        System.out.println("创建用户");
        // 发布事件，而不是直接调用
        eventPublisher.publishEvent(new UserCreatedEvent(this));
    }
}

@Service
public class OrderService {
    @EventListener
    public void handleUserCreated(UserCreatedEvent event) {
        System.out.println("用户创建事件处理，创建订单");
    }
}
```

### 5.2 使用@Lazy注解延迟加载

```java
@Service
public class UserService {
    
    @Lazy  // 延迟加载，避免循环依赖
    @Autowired
    private OrderService orderService;
    
    public void createUser() {
        System.out.println("创建用户");
        // 只有在真正使用时才会创建OrderService
        orderService.createOrder();
    }
}
```

### 5.3 使用@PostConstruct延迟初始化

```java
@Service
public class UserService {
    
    private OrderService orderService;
    
    @PostConstruct
    public void init() {
        // 在初始化阶段获取依赖，此时容器已经创建完成
        this.orderService = ApplicationContextHolder.getBean(OrderService.class);
    }
    
    public void createUser() {
        System.out.println("创建用户");
        orderService.createOrder();
    }
}
```

## 6. 循环依赖的检测和诊断

### 6.1 启用循环依赖检测

```properties
# application.properties
spring.main.allow-circular-references=true
```

### 6.2 循环依赖的日志分析

```java
@Component
public class CircularDependencyDetector implements BeanPostProcessor {
    
    private final Set<String> creatingBeans = new HashSet<>();
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (creatingBeans.contains(beanName)) {
            log.warn("检测到循环依赖: {}", beanName);
        }
        creatingBeans.add(beanName);
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        creatingBeans.remove(beanName);
        return bean;
    }
}
```

### 6.3 Bean生命周期监控

```java
@Component
public class BeanLifecycleMonitor implements BeanPostProcessor {
    
    private final Map<String, Long> beanStartTimes = new ConcurrentHashMap<>();
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        beanStartTimes.put(beanName, System.currentTimeMillis());
        log.info("Bean '{}' 开始初始化", beanName);
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Long startTime = beanStartTimes.get(beanName);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            log.info("Bean '{}' 初始化完成，耗时: {}ms", beanName, duration);
            
            // 检测初始化时间过长的Bean
            if (duration > 1000) {
                log.warn("Bean '{}' 初始化时间过长: {}ms", beanName, duration);
            }
        }
        return bean;
    }
}
```

## 7. 完整的Bean创建流程图

### 7.1 无循环依赖的Bean

```
Bean定义注册 → 实例化 → 属性注入 → 初始化 → 一级缓存 → 使用 → 销毁
```

### 7.2 有循环依赖的Bean

```
Bean定义注册 → 实例化 → 三级缓存 → 属性注入(触发循环依赖) → 二级缓存 → 初始化 → 一级缓存 → 使用 → 销毁
```

### 7.3 构造器循环依赖

```
Bean定义注册 → 实例化(构造器调用) → 需要依赖 → 依赖未创建 → 死锁 → 异常
```

### 7.4 原型Bean循环依赖

```
Bean定义注册 → 实例化 → 属性注入 → 需要依赖 → 创建新实例 → 无限递归 → 栈溢出
```

## 8. 总结

Spring通过三级缓存机制巧妙地解决了循环依赖问题：

1. **三级缓存** - 存储Bean的工厂对象，用于创建早期引用
2. **早期暴露** - 在Bean完全初始化前就暴露引用
3. **延迟注入** - 通过早期引用完成循环依赖的注入

**关键理解：**
- **不是所有Bean都经历三级缓存**：只有存在循环依赖的Bean才会使用完整的三级缓存机制
- **循环依赖解决时机**：在属性注入阶段，通过三级缓存提供早期引用
- **构造器循环依赖无法解决**：因为依赖注入发生在Bean实例化阶段，此时无法使用三级缓存机制
- **原型Bean循环依赖无法解决**：因为原型Bean每次都是新实例，不经过缓存，且三级缓存机制只对单例Bean有效

**最佳实践：**
- 尽量避免循环依赖，重新设计架构
- 使用接口解耦，依赖抽象而非具体实现
- 使用事件机制或观察者模式
- 合理使用@Lazy注解
- 在@PostConstruct中处理复杂依赖关系

这种机制让Spring能够优雅地处理循环依赖，同时保持了IoC容器的强大功能。通过理解Bean的完整生命周期和循环依赖的解决机制，开发者可以更好地设计Spring应用架构。
