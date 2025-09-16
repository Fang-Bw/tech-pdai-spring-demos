# Spring容器生命周期详解

基于 `003-spring-framework-demo-helloworld-anno` 项目示例

## 1. 项目结构分析

这个示例项目展示了Spring注解配置的基本用法：
- **配置类**: `BeansConfig.java` - 使用`@Configuration`注解
- **服务类**: `UserServiceImpl.java` - 使用`@Service`注解
- **DAO类**: `UserDaoImpl.java` - 使用`@Repository`注解
- **实体类**: `User.java` - 普通POJO类
- **主类**: `App.java` - 启动Spring容器

## 2. Spring容器启动流程

### 2.1 容器创建阶段

```java
// 在App.java中创建容器
AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
        "tech.pdai.springframework");
```

**详细步骤：**

1. **构造函数调用**
   - 创建`AnnotationConfigApplicationContext`实例
   - 初始化内部组件（如`BeanFactory`、`Environment`等）

2. **包扫描配置**
   - 扫描指定包路径：`"tech.pdai.springframework"`
   - 识别带有Spring注解的类

3. **Bean定义注册**
   - 解析`@Configuration`、`@Component`、`@Service`、`@Repository`等注解
   - 将Bean定义注册到`BeanDefinitionRegistry`中

### 2.2 Bean实例化阶段

**扫描到的Bean：**
- `BeansConfig` - 配置类
- `UserServiceImpl` - 服务类
- `UserDaoImpl` - DAO类

**实例化顺序：**
1. **配置类优先实例化** - `BeansConfig`
2. **依赖关系分析** - 分析`@Autowired`等依赖注入
3. **按依赖顺序实例化** - 先实例化`UserDaoImpl`，再实例化`UserServiceImpl`

### 2.3 依赖注入阶段

**在UserServiceImpl中的依赖注入：**
```java
@Service
public class UserServiceImpl {
    @Autowired
    private UserDaoImpl userDao;  // 自动注入UserDaoImpl实例
}
```

**注入过程：**
1. **查找依赖Bean** - 在容器中查找`UserDaoImpl`类型的Bean
2. **实例化依赖** - 如果依赖Bean未实例化，先实例化依赖Bean
3. **属性注入** - 通过反射将依赖Bean注入到目标Bean中

### 2.4 Bean初始化阶段

**初始化顺序：**
1. **构造方法执行** - 创建Bean实例
2. **属性设置** - 设置基本属性和依赖属性
3. **Aware接口回调** - 如果实现了`BeanNameAware`、`ApplicationContextAware`等接口
4. **BeanPostProcessor前置处理** - 执行所有`BeanPostProcessor`的`postProcessBeforeInitialization`方法
5. **@PostConstruct方法** - 如果存在`@PostConstruct`注解的方法
6. **InitializingBean.afterPropertiesSet()** - 如果实现了`InitializingBean`接口
7. **自定义init方法** - 如果配置了自定义初始化方法
8. **BeanPostProcessor后置处理** - 执行所有`BeanPostProcessor`的`postProcessAfterInitialization`方法

### 2.5 容器就绪阶段

**容器状态：**
- 所有Bean实例化完成
- 所有依赖注入完成
- 所有Bean初始化完成
- 容器可以正常使用

## 3. 具体执行流程示例

### 3.1 启动时序

```
1. 创建AnnotationConfigApplicationContext
   ↓
2. 扫描包"tech.pdai.springframework"
   ↓
3. 发现并注册Bean定义：
   - BeansConfig
   - UserServiceImpl  
   - UserDaoImpl
   ↓
4. 实例化BeansConfig（配置类优先）
   ↓
5. 实例化UserDaoImpl（无依赖）
   ↓
6. 实例化UserServiceImpl（依赖UserDaoImpl）
   ↓
7. 注入UserDaoImpl到UserServiceImpl
   ↓
8. 容器就绪，可以获取Bean使用
```

### 3.2 Bean获取和使用

```java
// 从容器中获取Bean
UserServiceImpl service = context.getBean(UserServiceImpl.class);

// 使用Bean
List<User> userList = service.findUserList();
```

## 4. 生命周期扩展点

### 4.1 可添加的生命周期回调

**初始化回调：**
```java
@PostConstruct
public void init() {
    // 在依赖注入完成后执行
    System.out.println("UserServiceImpl initialized");
}
```

**销毁回调：**
```java
@PreDestroy
public void destroy() {
    // 在Bean销毁前执行
    System.out.println("UserServiceImpl destroying");
}
```

**Aware接口：**
```java
public class UserServiceImpl implements BeanNameAware, ApplicationContextAware {
    @Override
    public void setBeanName(String name) {
        // 获取Bean名称
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        // 获取ApplicationContext
    }
}
```

### 4.2 BeanPostProcessor示例

```java
@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // Bean初始化前的处理
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // Bean初始化后的处理
        return bean;
    }
}
```

## 5. 容器关闭阶段

**关闭方式：**
```java
// 方式1：注册关闭钩子
context.registerShutdownHook();

// 方式2：手动关闭
context.close();
```

**关闭过程：**
1. **@PreDestroy方法执行** - 执行所有Bean的`@PreDestroy`方法
2. **DisposableBean.destroy()** - 如果实现了`DisposableBean`接口
3. **自定义destroy方法** - 如果配置了自定义销毁方法
4. **资源释放** - 释放容器占用的资源

## 6. 总结

Spring容器的生命周期可以概括为：

**启动阶段：**
- 容器创建 → 包扫描 → Bean定义注册 → Bean实例化 → 依赖注入 → Bean初始化 → 容器就绪

**运行阶段：**
- Bean使用和管理

**关闭阶段：**
- 执行销毁回调 → 资源释放 → 容器关闭

这个示例项目展示了Spring容器生命周期的核心流程，通过注解配置简化了传统的XML配置方式，让开发者能够更直观地理解Spring的IoC容器工作原理。
