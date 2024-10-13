## 1.aop实现过程
```
（1）使用AspectJ作为实现方法，动态织入，即在运行时插入新增逻辑
（2）如果使用接口，则用 JDK 的动态代理实现；如果没有实现接口，则使用 CGLIB 通过字节码技术来实现
（3）3个关键要素：切点、通知、连接点（切面包含切点和通知）
    1）通知（增强）
        前置增强（Before Advice）：在目标方法执行前进行处理；
        后置增强（After Advice）：在目标方法执行后执行，无论成功与否；
        返回后增强（After Returning Advice）：在目标方法成功执行后处理返回值；
        异常后增强（After Throwing Advice）：在目标方法抛出异常时执行处理；
        环绕增强（Around Advice）：可控制目标方法的执行，前后均可处理
    2）切点：定义了在哪些连接点上应用通知的表达式
        方法执行表达式（execution）：匹配方法的执行，可以指定类、方法名、参数等。
            示例：execution(* com.example.service.*.*(..))
        注解表达式（@annotation）：匹配被特定注解标注的方法。
            示例：@annotation(org.springframework.web.bind.annotation.PostMapping)
        类类型表达式（within）：匹配特定类型的类或其子类中的方法。
            示例：within(com.example.service.*)
        bean 表达式（bean）：匹配 Spring 容器中指定名称的 bean。
            示例：bean(myService)
        切入点表达式（@pointcut）：定义切入点，可以组合多种表达式，便于复用。
            示例：@Pointcut("execution(* com.example.service.*.*(..))")
    3）连接点：程序执行的一个点，如方法调用
```
## 2.实现原理
### https://www.cnblogs.com/diandiandidi/p/14559018.html
### https://developer.aliyun.com/article/1248003
### https://pdai.tech/md/spring/spring-x-framework-aop-source-2.html
```
一般切面的生成 使用 注解切面代理创建类(AnnotationAwareAspectJAutoProxyCreator) 实现
（1）postProcessBeforeInstantiation方法：主要是处理使用了@Aspect注解的切面类，然后将切面类的所有切面方法根据使用的注解生成对应Advice，并将Advice连同切入点匹配器和切面类等信息一并封装到Advisor
（2）postProcessAfterInitialization：判断bean是否与advosor匹配，将Advisor注入到合适的位置，创建代理（cglib或jdk)，。
（3）被代理类初始化完成之后，生成其代理类放入ioc容器中
```
## 3.代码debug cglib生成的代理类为例（使用的角度）
```
（1）实际使用的类是代理类为CglibAopProxy
（2）获取配置好的通知（advice）
（3）通过CglibMethodInvocation来启动advice通知
```

## 4.其他
```
(1)针对同一方法的切面顺序，使用@Order实现
(2)Spring AOP如何处理事务
```

## 5.常见问题
```
(1)当在被代理类内部一个方法调用另一个被 AOP 增强的方法时，切面不会生效；同理不能增强构造函数。 原因：Spring AOP 使用的是代理模式，如果方法调用没有通过代理对象，而是直接在类内部调用，切面无法拦截
(2)final 方法或类无法被 Spring AOP 增强，增强无效
(3)在 AOP 切面中注入依赖的对象时，依赖没有正确注入，导致 NullPointerException
(4)在切面中使用非线程安全的变量，导致在多线程环境下出现数据不一致的问题
(5)aop只能增强public和protected方法，缺省和private修饰的不行
```