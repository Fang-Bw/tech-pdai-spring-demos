## Spring Bean的作用域

---

### 总结概览

| 作用域(Scope) | 声明方式 | 生命周期 | 核心使用场景 | 线程安全性 |
| :--- | :--- | :--- | :--- | :--- |
| **singleton** | 默认 / `@Scope("singleton")` | 容器启动/首次请求 -> 容器销毁 | **无状态服务**：Service, DAO, 工具类, 配置类, 组件 | 必须自行保证（因为共享） |
| **prototype** | `@Scope("prototype")` | 每次请求时创建 -> 由GC回收 | **有状态对象**：需要隔离的上下文对象、计算过程 | 不关心（每个线程用自己实例） |
| **request** | `@Scope("request")` / `@RequestScope` | HTTP请求开始 -> 请求结束 | **请求级数据**：表单数据、查询参数、请求追踪ID | 安全（每个请求独立） |
| **session** | `@Scope("session")` / `@SessionScope` | Session创建 -> Session过期 | **用户级数据**：用户登录信息、购物车、偏好设置 | 安全（每个会话独立） |
| **application**| `@Scope("application")` | Web应用启动 -> 应用关闭 | **应用级缓存**：全局配置、共享资源、只读缓存 | 必须自行保证（因为共享） |
| **websocket** | `@Scope("websocket")` | WebSocket会话建立 -> 会话关闭 | **Socket会话数据**：实时游戏状态、聊天会话 | 安全（每个连接独立） |

---

### 1. 单例模式 (Singleton Scope)

#### 使用场景
*   **核心业务逻辑层**：如 `UserService`, `OrderService`, `ProductDao` 等。这些组件通常是无状态的，只包含业务方法。
*   **工具类/辅助类**：如 `DateUtils`, `StringHelper`, `JsonConverter` 等。它们没有状态，只需要一份实例提供方法。
*   **配置类与组件**：如 `@Configuration` 类中通过 `@Bean` 定义的数据源 (`DataSource`)、事务管理器 (`PlatformTransactionManager`)。这些资源通常昂贵且全局只需一份。
*   **缓存管理器**：如 Spring 的 `CacheManager`，需要全局统一管理缓存。

#### 如何使用
**方式一：默认（最常用）**
Spring 默认就是单例，无需任何额外注解。
```java
@Service // 默认就是单例
public class UserServiceImpl implements UserService {
    // 业务方法...
}
```

**方式二：显式声明**
```java
@Component
@Scope("singleton") // 显式声明，与默认效果相同
public class MyUtility {
    // ...
}
```

**方式三：XML配置**
```xml
<bean id="userService" class="com.example.service.UserServiceImpl" scope="singleton"/>
```

#### 注意事项
*   **线程安全**：由于是共享实例，**必须确保该类是无状态的**（不使用可变的实例变量），或者自行实现线程安全（如使用同步锁、并发集合等），否则会导致数据错乱。

---

### 2. 原型模式 (Prototype Scope)

#### 使用场景
*   **有状态的上下文对象**：例如一个 `OrderCalculationContext`，它在计算过程中会不断修改折扣、税费等状态，每次计算都需要一个全新的、隔离的上下文。
*   **需要隔离的流程组件**：某些执行流程的 Bean，每次执行都会修改内部状态，不适合共享。
*   **非线程安全的对象**：如果你需要注入一个非线程安全的第三方库对象（并且该对象不能配置为单例），可以使用原型模式每次获取一个新的。

#### 如何使用
**使用 `@Scope` 注解**
```java
@Component
@Scope("prototype") // 关键注解
public class ShoppingCart {
    private List<Item> items = new ArrayList<>(); // 有状态

    public void addItem(Item item) {
        items.add(item);
    }
    // ...
}
```

**注入和使用（重点！）**
由于原型Bean每次都需要新的，不能直接使用`@Autowired`字段注入（因为注入只在初始化时发生一次）。

**正确方式1：通过 ApplicationContext 实时获取**
```java
@Service
public class OrderService {
    @Autowired
    private ApplicationContext applicationContext; // 注入容器

    public void checkout() {
        // 每次调用getBean都会返回一个新的ShoppingCart实例
        ShoppingCart cart = applicationContext.getBean(ShoppingCart.class);
        cart.addItem(...);
        // ... 处理订单
        // cart实例使用完后由GC管理，容器不再负责其销毁
    }
}
```

**正确方式2：使用 `@Lookup` 方法注入（较少用）**
```java
@Service
public abstract class OrderService {

    // Spring会重写此方法，每次调用都返回新的原型Bean
    @Lookup
    protected abstract ShoppingCart createShoppingCart();

    public void checkout() {
        ShoppingCart cart = createShoppingCart();
        cart.addItem(...);
        // ...
    }
}
```

**XML配置**
```xml
<bean id="shoppingCart" class="com.example.ShoppingCart" scope="prototype"/>
```

---

### 3. 请求(Request) & 会话(Session) & 应用(Application) Scope

这些作用域需要Web环境支持。确保你的项目是Web项目（如Spring Boot Web），无需额外配置即可使用。

#### 请求作用域 (Request Scope)
**使用场景**：存储与**一次特定HTTP请求**相关的信息。
*   表单绑定对象（如 `UserForm`）
*   请求的追踪ID（`requestId`）用于日志串联
*   本次请求需要处理的临时数据

**如何使用：**
```java
@Component
@RequestScope // 或 @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ApiRequestContext {
    private final String requestId = UUID.randomUUID().toString();

    public String getRequestId() {
        return requestId;
    }
}

// 在Controller或Service中注入
@RestController
public class MyController {
    // 每个请求注入的都是该请求独有的ApiRequestContext实例
    @Autowired
    private ApiRequestContext requestContext;

    @GetMapping("/test")
    public String test() {
        return "Request ID: " + requestContext.getRequestId();
    }
}
```

#### 会话作用域 (Session Scope)
**使用场景**：存储与**一个用户会话**相关的信息。
*   用户登录信息（`UserProfile`）
*   购物车内容（`ShoppingCart`）
*   用户的语言、主题等个性化设置

**如何使用：**
```java
@Component
@SessionScope // 或 @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserSession {
    private User loggedInUser;
    private Locale userLocale;

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }
    public User getLoggedInUser() {
        return loggedInUser;
    }
    // ... getters and setters
}

// 在Controller中使用
@Controller
public class AuthController {

    @Autowired
    private UserSession userSession;

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        User user = userService.authenticate(request);
        userSession.setLoggedInUser(user); // 设置到会话中
        return "redirect:/dashboard";
    }
}
```

#### 应用作用域 (Application Scope)
**使用场景**：存储**整个Web应用**级别的全局、只读或线程安全的数据。
*   应用配置信息（从数据库或文件加载后缓存）
*   共享的只读数据字典（如国家、城市列表）
*   内存中的计数器（需要线程安全）

**如何使用：**
```java
@Component
@Scope(value = WebApplicationContext.SCOPE_APPLICATION)
public class ApplicationCache {
    private final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<>();

    public void put(String key, Object value) {
        cache.put(key, value);
    }

    public Object get(String key) {
        return cache.get(key);
    }
}
```

---

### 4. WebSocket作用域 (WebSocket Scope)

#### 使用场景
存储与**一个WebSocket会话**相关的信息。生命周期与WebSocket连接保持一致。
*   实时游戏的玩家状态
*   聊天室中的单个聊天会话上下文
*    websocket连接中的特定数据

#### 如何使用
```java
@Component
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WebSocketSessionState {
    private String sessionId;
    private GameState gameState; // 例如，存储游戏状态

    @PreDestroy
    public void destroy() {
        // 连接关闭时清理资源
    }
}
// 通常在@MessageMapping注解的方法中注入使用
```

### 关键配置说明：`proxyMode`

在注入 Request/Session/Application/WebSocket 等作用域比 Singleton/Prototype 更短的 Bean 到长生命周期的 Bean（如 Singleton 的 Service）中时，必须使用代理（`proxyMode`）。

*   **`ScopedProxyMode.TARGET_CLASS`**：基于CGLIB创建目标类的代理。适用于类。
*   **`ScopedProxyMode.INTERFACES`**：基于JDK动态代理创建接口的代理。适用于接口。

使用 `@RequestScope`、`@SessionScope` 等注解时，Spring 会自动帮你配置好代理，通常无需手动设置。如果使用 `@Scope(value = "...")`，则需要显式设置 `proxyMode`。