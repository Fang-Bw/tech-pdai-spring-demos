# Spring事务
Spring事务实现的核心思想：将事务管理作为一个横切关注点（Aspect），通过AOP的方式与业务代码进行解耦

## 1.Spring事务的实现机制
### AOP


## 2.核心接口与关键组件
### 2.1 PlatformTransactionManager
事务策略的抽象，定义了事务的基本操作
- TransactionStatus getTransaction(@Nullable TransactionDefinition definition)： 获取事务的状态（开启一个新事务或加入当前存在的事务）。
- void commit(TransactionStatus status)： 提交事务。
- void rollback(TransactionStatus status)： 回滚事务。

不同的持久层技术有不同的实现

### 2.2 TransactionDefinition
定义了事务的属性
#### 2.2.1 关键属性
- 传播行为 (Propagation Behavior)： 最关键的特性之一，定义了事务与事务之间的关系。例如：当前方法没有事务时怎么办？有事务时又怎么办？
- 隔离级别 (Isolation Level)： 定义了事务与其他事务的隔离程度，解决脏读、不可重复读、幻读等问题。（与数据库的隔离级别概念一致）
- 超时时间 (Timeout)： 事务执行的最长时间，超时则自动回滚。
- 是否只读 (Read-only)： 提示数据库该事务只进行读操作，允许数据库进行优化。对写操作强制设置为只读会抛出异常

#### 2.2.2 传播行为
| 传播行为类型 | 说明 |
| :--- | :--- |
| **`REQUIRED`** (**默认**) | 如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新事务。 |
| **`REQUIRES_NEW`** | 创建一个新的事务，如果当前存在事务，则**挂起**当前事务。两个事务相互独立，互不干扰。 |
| **`SUPPORTS`** | 如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。 |
| **`NOT_SUPPORTED`** | 以非事务方式运行，如果当前存在事务，则挂起当前事务。 |
| **`MANDATORY`** | 如果当前存在事务，则加入该事务；如果当前没有事务，则**抛出异常**。 |
| **`NEVER`** | 以非事务方式运行，如果当前存在事务，则**抛出异常**。 |
| **`NESTED`** | 如果当前存在事务，则在当前事务的嵌套事务内执行；如果当前没有事务，则行为与 `REQUIRED` 类似。**嵌套事务是外部事务的一部分，只有外部事务提交了，嵌套事务的提交才有效。外部事务回滚，嵌套事务也会回滚；而嵌套事务回滚，不会影响外部事务（除非设置了保存点）**。 |

### 2.3 TransactionStatus
表示事务在运行时的状态

### 2.4 TransactionInterceptor


### 2.5 事务上下文
使用ThreadLocal将事务上下文（主要是数据库连接）和当前线程绑定，一个事务中的所有操作都通过一个连接进行

## 3. 事务的使用
通常使用声明式（注解）的方式使用事务

### 3.1 使用步骤
- 启用：通过 @EnableTransactionManagement 启用声明式事务功能。
- 装配：Spring容器为此自动装配核心组件，特别是注册一个 BeanFactoryTransactionAttributeSourceAdvisor。该顾问持有：
  - 事务属性源 (TransactionAttributeSource)：用于解析 @Transactional 注解的属性。
  - 事务拦截器 (TransactionInterceptor)：包含事务处理逻辑的增强。
- 代理：在Bean创建时，Spring AOP机制会为那些匹配顾问中切点规则（即拥有 @Transactional 注解）的Bean创建代理对象。

- 拦截与事务管理：
  - 当调用代理对象的事务方法时，TransactionInterceptor 被触发。
  - 拦截器根据方法的传播行为等属性，委托 PlatformTransactionManager 处理事务（可能需要新开事务、挂起事务或加入现有事务）
  - 拦截器通过反射调用原始目标对象的业务方法。
  
- 决策与执行：
  - 业务方法执行完毕后，拦截器根据执行结果（成功完成还是抛出异常）以及配置的回滚规则做出提交或回滚的决策。
  - 拦截器再次委托 PlatformTransactionManager 来最终执行 commit 或 rollback 操作。
  - 事务管理器执行操作后，会清理资源（如解绑连接、归还连接池）。

### 3.2 不同事务传播机制的实现

#### 场景一：当前**存在**活动事务 (Existing Transaction)
| 传播行为 | 处理逻辑 |
| :--- | :--- |
| **`PROPAGATION_REQUIRED` (默认)** | **加入当前事务**。不会创建新事务，直接使用现有的连接和事务上下文。共用同一个事务，一起提交或回滚。 |
| **`PROPAGATION_REQUIRES_NEW`** | **挂起当前事务** -> **创建新事务**。<br>1. **挂起(Suspend)**：将当前事务信息（如连接）从线程绑定中取出并保存到一个 `SuspendedResourcesHolder` 对象中。<br>2. **新建**：创建一个全新的、独立的事务（获取新连接，`setAutoCommit(false)`）。<br>3. 新事务执行完毕后再**恢复(Resume)** 被挂起的事务。 |
| **`PROPAGATION_SUPPORTS`** | **加入当前事务**。以事务方式执行。 |
| **`PROPAGATION_NOT_SUPPORTED`** | **挂起当前事务** -> **以非事务方式运行**。当前方法不在事务中执行，使用自动提交模式。执行完后恢复原事务。 |
| **`PROPAGATION_MANDATORY`** | **加入当前事务**。如果不存在当前事务则**抛出异常**。 |
| **`PROPAGATION_NEVER`** | **抛出异常**。因为当前存在事务，而传播行为要求不能有事务。 |
| **`PROPAGATION_NESTED`** | **创建嵌套事务（保存点）**。<br>在当前事务内部创建一个**保存点(Savepoint)**。这是一种特殊的“嵌套”事务。如果子方法回滚，则只回滚到保存点，不影响外部事务的其他操作；外部事务回滚，则嵌套事务一定回滚。 |

#### 场景二：当前**不存在**活动事务 (No Existing Transaction)
| 传播行为 | 处理逻辑 |
| :--- | :--- |
| **`PROPAGATION_REQUIRED`** | **创建新事务**。这是最常见的情况。 |
| **`PROPAGATION_REQUIRES_NEW`** | **创建新事务**。 |
| **`PROPAGATION_SUPPORTS`** | **以非事务方式执行**。不创建事务，使用自动提交模式。 |
| **`PROPAGATION_NOT_SUPPORTED`** | **以非事务方式执行**。 |
| **`PROPAGATION_MANDATORY`** | **抛出异常**。因为当前没有事务，而传播行为要求必须存在事务。 |
| **`PROPAGATION_NEVER`** | **以非事务方式执行**。 |
| **`PROPAGATION_NESTED`** | **创建新事务**。注意：如果底层数据库不支持保存点，则行为与 `PROPAGATION_REQUIRED` 相同。 |

## 4. 常见问题
- 1.@Transactional 生效的前提： 基于Spring AOP。因此：
    - 同类调用失效： 在同一个类中，一个非事务方法 a() 基于this调用一个事务方法 b()，事务不会生效。因为 a() 内部调用的 this.b() 不是代理对象的方法。
- 2.默认只回滚运行时异常和 Error： @Transactional 默认只在抛出 运行时异常（RuntimeException） 和 Error 时回滚，受检异常（Checked Exception，Exception 的子类，但不是 RuntimeException 的子类） 不会导致回滚。
  - 如果需要受检异常也回滚，使用 @Transactional(rollbackFor = Exception.class)。
- 3.事务的上下文（绑定到ThreadLocal的连接和状态）是线程隔离的，不能跨线程共享，因此一个事务只能由一个线程来执行和完成
  - 如果在一个事务方法中启动一个新线程（new Thread(...).start() 或通过线程池提交数据库任务），则该线程的操作不具有事务性
- 4.将@Transactional注解放在实现类上，而不是接口上
  - @Transactional注解包含了@Inherited注解，但是@Inherited注解不会在接口上生效
  - @Transactional注解放在接口上，则事务失效；@Transactional注解放在接口方法上，且AOP使用的是基于CGLIB，则事务失效