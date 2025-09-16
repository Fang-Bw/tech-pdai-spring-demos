# Java的数据持久化
- JPA 是一种ORM规范，Hibernate是它的一种实现
- MyBatis 是SQL映射框架

| 特性 | JPA (Hibernate, EclipseLink) | MyBatis |
| :--- | :--- | :--- |
| **本质** | **ORM 框架** | **SQL 映射框架** |
| **SQL 生成** | **自动生成** | **手动编写** |
| **灵活性** | 相对较低，复杂查询较麻烦 | **极高**，可以编写任意复杂、高度优化的SQL |
| **开发效率** | **高**，简单CRUD极快 | 中等，无论简单复杂都需要编写SQL和映射 |
| **学习曲线** | 较陡峭，需理解缓存、延迟加载等概念 | 相对平缓，对于熟悉SQL的开发者更友好 |
| **性能控制** | 需要了解框架原理才能优化 | **直接通过SQL控制**，易于优化 |
| **移植性** | **高**，更换数据库时代码改动小 | **低**，不同数据库的SQL语法可能不同，需要修改XML |

## 1.JPA
## 1.1 核心组件

| 组件名称 | 描述与作用 | 核心接口 / 注解 | 代码示例 |
| :--- | :--- | :--- | :--- |
| **实体 (Entity)** | 与数据库表映射的普通 Java 对象 (POJO)，一个实例代表表中的一行数据。 | `@Entity`, `@Id`, `@Table`, `@Column`, `@GeneratedValue` | 见下方示例 ↓ |
| **实体管理器工厂 (EntityManagerFactory)** | 重量级、线程安全的工厂，用于创建 `EntityManager` 实例。是初始化 JPA 的起点，通常一个数据库对应一个 EMF。 | `Persistence`, `EntityManagerFactory` | 见下方示例 ↓ |
| **实体管理器 (EntityManager)** | **核心接口**，用于管理实体生命周期（CRUD）和与**持久化上下文**交互。每个实例通常对应一个数据库事务或会话。 | `EntityManager` | 见下方示例 ↓ |
| **持久化单元 (Persistence Unit)** | 一组实体类、数据库连接和配置的逻辑集合。是 `persistence.xml` 中的配置单元。 | `<persistence-unit>` (在 `persistence.xml` 中) | 见下方示例 ↓ |
| **持久化上下文 (Persistence Context)** | 由 `EntityManager` 管理的**实体实例的集合（一级缓存）**。它跟踪所有托管实体的状态，并负责**脏检查**和**延迟加载**。 | (由 `EntityManager` 内部管理) | 见下方示例 ↓ |
| **JPQL (查询语言)** | 面向对象的查询语言。操作的是**实体和属性**，而非数据库表和字段。用于编写跨数据库的平台无关查询。 | `EntityManager.createQuery()` | 见下方示例 ↓ |
| **Criteria API** | 提供一种类型安全、面向对象的方式动态创建查询，避免了 JPQL 字符串拼接的风险。 | `CriteriaBuilder`, `CriteriaQuery` | 见下方示例 ↓ |
| **事务管理 (Transaction)** | 管理数据库操作的原子性。JPA 支持两种事务类型：JTA（全局事务）和 RESOURCE_LOCAL（本地事务）。 | `EntityTransaction` | 见下方示例 ↓ |

### 1.1.1 实体 (Entity) 示例
```java
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String name;
}
```

### 1.1.2 实体管理器工厂 (EntityManagerFactory) 示例
```java
EntityManagerFactory emf = 
    Persistence.createEntityManagerFactory(
        "my-persistence-unit");
```

### 1.1.3 实体管理器 (EntityManager) 示例
```java
EntityManager em = emf.createEntityManager();
User user = em.find(User.class, 1L);
em.persist(newUser);
```

### 1.1.4 持久化单元 (Persistence Unit) 示例
```xml
<persistence-unit name="myAppPU">
    <class>com.example.User</class>
    ...properties...
</persistence-unit>
```

### 1.1.5 持久化上下文 (Persistence Context) 示例
```java
// em 内部维护着一个Map<Id, Entity>
User u1 = em.find(User.class, 1L);
User u2 = em.find(User.class, 1L);
// u1 == u2, 保证了唯一性
```

### 1.1.6 JPQL (查询语言) 示例
```java
String jpql = "SELECT u FROM User u WHERE u.name LIKE :name";
TypedQuery<User> q = em.createQuery(jpql, User.class);
q.setParameter("name", "%John%");
List<User> users = q.getResultList();
```

### 1.1.7 Criteria API 示例
```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<User> cq = cb.createQuery(User.class);
Root<User> root = cq.from(User.class);
cq.select(root).where(cb.equal(root.get("name"), "John"));
```

### 1.1.8 事务管理 (Transaction) 示例
```java
EntityTransaction tx = em.getTransaction();
try {
    tx.begin();
    em.persist(user); // 业务操作
    tx.commit(); // 提交并自动flush
} catch (Exception e) {
    tx.rollback();
}
```

## 1.2 JPA的实现
Spring-data模块包含各类数据库的使用（以Spring-data-commons为基础），其中Spring-data-jpa是基于spring生态的jpa使用

### 1.2.1 Spring-data-jpa

相关接口的体系结构如下，提供不同的查询能力抽象
```
Repository (标记接口)
    ↓
CrudRepository (基础CRUD)
    ↓
PagingAndSortingRepository (分页排序)
    ↓
JpaRepository (JPA特性)
    ↓
JpaSpecificationExecutor (动态查询)
```

本质是为Repository标记的接口生成代理对象，实际的查询通过代理对象完成

## 2.Mybatis
