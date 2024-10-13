## 1.bean大致使用流程
```
（1）bean信息的加载和解析（ResourceLoader、BeanDefinitionReader）
（2）bean实例的生成并放到容器中
（3）特殊bean的加载（如国际化相关的bean）
（4）bean的使用和管理
```

## 2.相关组件
```
（1）BeanFactory定义了ioc容器的规范
（2）BeanDefinition是定义bean信息的类
（3）ApplicationContext接口
```

## 3.Bean的生命周期(https://youle.zhipin.com/articles/aed0e15afefebf8aqxB709u4EQ~~.html)
```
（1）阶段：实例化--属性赋值--初始化--使用--销毁
（2）实例化：步骤如下
    1）使用BeanDefinitionReader读取bean的定义，生成BeanDefinition，注册到BeanDefinitionRegistry中；
    2）使用反射或cglib方式创建实例，创建到SingletionObjects中；具体的实例化时间由是否懒加载决定（策略模式）
（3）属性赋值：不同类型的属性赋值方式不一样
    1）基本类型：按照定义赋初始值或零值
    2）引用类型：查找依赖的对象是否纳入容器的管理，如果没有则先初始化依赖的对象；使用Aware接口（如@Autowired）可以自动注入依赖的对象；注意循环依赖如何解决
    3）Spring提供BeanFactory和ApplicationContext（BeanFactory的子类）两类IOC容器
    4）Aware接口的作用是让Bean能拿到容器的一些资源；如BeanNameAeare是让bean获取名字在业务逻辑中使用，BeanFactoryAware直接使用BeanFactory，ApplicationContextAware直接使用ApplicationContext
    5）一些后处理器，作用就是进行一些前置和后置的处理，如BeanPostProcessor进行实例化前后的处理
    6）InitializingBean和DisposableBean 接口就是用来定义初始化方法和销毁方法
    7）自定义init方法
（4）使用
（5）销毁
```


## 4.Bean的生命周期的每个阶段可以自定义什么逻辑？
```
（1）依赖注入完成之后可以使用@PostConstruct注解标记的方法进行 加载配置信息、校验依赖或启动某些后台任务 等
（2）销毁之前使用@PreDestroy进行资源的关闭或释放
```

## 5.Bean的生命周期的aop阶段