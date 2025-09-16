# Spring 分布式事务示例

本项目演示如何在Spring Boot中使用JTA（Java Transaction API）和Atomikos实现分布式事务。

## 项目概述

该示例模拟了一个电商场景，涉及用户账户和订单两个独立的数据库：
- **用户数据库（user_db）**：存储用户信息和账户余额
- **订单数据库（order_db）**：存储订单信息

当用户下单时，需要同时：
1. 在订单数据库中创建订单记录
2. 在用户数据库中扣减用户余额

这两个操作必须在同一个分布式事务中完成，确保数据一致性。

## 技术栈

- **Spring Boot 2.6.13**
- **Spring Data JPA**
- **Atomikos JTA**：分布式事务管理器
- **MySQL 8.0**：数据库
- **Lombok**：简化代码

## 项目结构

```
src/main/java/com/example/springdistributetransaction/
├── config/
│   ├── DataSourceConfig.java      # 多数据源配置
│   └── JpaConfig.java            # JPA配置
├── entity/
│   ├── user/User.java            # 用户实体
│   └── order/Order.java          # 订单实体
├── repository/
│   ├── user/UserRepository.java  # 用户数据访问层
│   └── order/OrderRepository.java # 订单数据访问层
├── service/
│   ├── UserService.java          # 用户服务
│   └── OrderService.java         # 订单服务（分布式事务核心）
├── controller/
│   ├── UserController.java       # 用户控制器
│   ├── OrderController.java      # 订单控制器
│   └── TestController.java       # 测试控制器
└── SpringDistributeTransactionApplication.java
```

## 核心配置

### 1. 多数据源配置（DataSourceConfig.java）

配置两个Atomikos数据源：
- `userDataSource`：用户数据库连接
- `orderDataSource`：订单数据库连接

### 2. JTA事务管理器

使用Atomikos作为JTA事务管理器，确保跨数据源的事务一致性。

### 3. JPA配置（JpaConfig.java）

为不同的数据源配置独立的EntityManagerFactory和Repository扫描。

## 数据库准备

在MySQL中创建两个数据库：

```sql
-- 创建用户数据库
CREATE DATABASE user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建订单数据库
CREATE DATABASE order_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

应用启动时会自动创建表结构。

## 运行项目

1. 确保MySQL服务运行，并创建了上述数据库
2. 修改`application.yml`中的数据库连接信息
3. 运行`SpringDistributeTransactionApplication`
4. 访问 http://localhost:8080

## API接口

### 测试接口

- `POST /api/test/init-data` - 初始化测试数据
- `POST /api/test/transaction-success` - 测试分布式事务成功场景
- `POST /api/test/transaction-rollback` - 测试分布式事务回滚场景
- `GET /api/test/status` - 查看当前数据状态

### 用户接口

- `POST /api/users` - 创建用户
- `GET /api/users` - 查询所有用户
- `GET /api/users/{userId}` - 根据ID查询用户
- `POST /api/users/{userId}/recharge` - 用户充值

### 订单接口

- `POST /api/orders` - 创建订单（分布式事务）
- `POST /api/orders/with-exception` - 创建订单并模拟异常（测试回滚）
- `GET /api/orders` - 查询所有订单
- `POST /api/orders/{orderId}/cancel` - 取消订单

## 分布式事务测试

### 1. 初始化测试数据

```bash
curl -X POST http://localhost:8080/api/test/init-data
```

### 2. 测试正常事务

```bash
curl -X POST http://localhost:8080/api/test/transaction-success
```

这将创建一个订单并扣减用户余额，两个操作在同一个分布式事务中完成。

### 3. 测试事务回滚

```bash
curl -X POST http://localhost:8080/api/test/transaction-rollback
```

这将模拟业务异常，验证分布式事务是否正确回滚。

### 4. 查看状态

```bash
curl -X GET http://localhost:8080/api/test/status
```

## 关键代码说明

### OrderService.createOrder()

```java
@Transactional
public Order createOrder(Long userId, String productName, BigDecimal amount, Integer quantity) {
    // 1. 检查用户余额
    // 2. 扣减用户余额（操作用户数据库）
    // 3. 创建订单（操作订单数据库）
    // 整个方法在一个分布式事务中执行
}
```

### 事务回滚测试

```java
@Transactional
public Order createOrderWithException(...) {
    // 执行业务逻辑
    // 最后抛出异常，触发分布式事务回滚
    throw new RuntimeException("模拟业务异常，测试分布式事务回滚");
}
```

## 注意事项

1. **数据库支持**：确保MySQL启用了XA事务支持
2. **连接池配置**：Atomikos数据源的连接池参数需要合理配置
3. **事务超时**：根据业务需要调整事务超时时间
4. **日志配置**：开启Atomikos和事务相关日志便于调试
5. **异常处理**：分布式事务中的异常会触发回滚，需要合理处理

## 监控和日志

项目配置了详细的日志输出：
- 事务执行日志
- SQL执行日志
- Atomikos事务管理器日志

日志文件位置：`./logs/application.log`

## 扩展

可以基于此示例扩展：
- 添加更多数据源
- 集成消息队列实现最终一致性
- 添加分布式锁
- 集成Seata等分布式事务中间件