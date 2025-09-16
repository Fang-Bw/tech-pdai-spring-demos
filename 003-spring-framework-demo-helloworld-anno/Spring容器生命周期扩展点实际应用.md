# Spring容器生命周期扩展点实际应用

基于Spring容器的生命周期扩展点，在实际业务开发中可以实现多种功能。以下是具体的应用场景和实现方式：

## 1. BeanPostProcessor 扩展点应用

### 1.1 性能监控和统计

```java
@Component
public class PerformanceMonitorBeanPostProcessor implements BeanPostProcessor {
    
    private final Map<String, Long> beanCreationTimes = new ConcurrentHashMap<>();
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        beanCreationTimes.put(beanName, System.currentTimeMillis());
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Long startTime = beanCreationTimes.get(beanName);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            log.info("Bean '{}' 初始化耗时: {}ms", beanName, duration);
            
            // 记录到监控系统
            if (duration > 1000) {
                alertService.sendAlert("Bean初始化过慢: " + beanName + ", 耗时: " + duration + "ms");
            }
        }
        return bean;
    }
}
```

### 1.2 数据源动态切换

```java
@Component
public class DataSourceSwitchBeanPostProcessor implements BeanPostProcessor {
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource) {
            // 根据当前租户或用户上下文动态切换数据源
            String currentTenant = TenantContext.getCurrentTenant();
            if ("tenant1".equals(currentTenant)) {
                return switchToTenant1DataSource((DataSource) bean);
            } else if ("tenant2".equals(currentTenant)) {
                return switchToTenant2DataSource((DataSource) bean);
            }
        }
        return bean;
    }
}
```

### 1.3 缓存预热

```java
@Component
public class CacheWarmupBeanPostProcessor implements BeanPostProcessor {
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof CacheManager) {
            // 系统启动后预热常用缓存
            warmupCache((CacheManager) bean);
        }
        return bean;
    }
    
    private void warmupCache(CacheManager cacheManager) {
        // 预热用户权限缓存
        cacheManager.getCache("userPermissions").put("admin", loadAdminPermissions());
        // 预热系统配置缓存
        cacheManager.getCache("systemConfig").put("global", loadGlobalConfig());
    }
}
```

## 2. Aware接口扩展点应用

### 2.1 获取容器资源

```java
@Service
public class SystemMonitorService implements ApplicationContextAware, BeanNameAware {
    
    private ApplicationContext applicationContext;
    private String beanName;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        // 获取容器中所有Bean信息
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        log.info("容器中共有 {} 个Bean", beanNames.length);
        
        // 获取环境信息
        Environment env = applicationContext.getEnvironment();
        String activeProfile = env.getActiveProfiles().length > 0 ? env.getActiveProfiles()[0] : "default";
        log.info("当前激活的Profile: {}", activeProfile);
    }
    
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        log.info("当前Bean名称: {}", name);
    }
    
    public void monitorSystemStatus() {
        // 监控系统状态
        Map<String, Object> status = new HashMap<>();
        status.put("beanCount", applicationContext.getBeanDefinitionCount());
        status.put("beanName", beanName);
        status.put("startTime", System.currentTimeMillis());
        
        // 发送监控数据
        monitoringService.sendMetrics(status);
    }
}
```

### 2.2 动态获取配置

```java
@Service
public class DynamicConfigService implements EnvironmentAware {
    
    private Environment environment;
    
    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        // 监听配置变化
        if (environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment configEnv = (ConfigurableEnvironment) environment;
            configEnv.addPropertySource(new FilePropertySource("dynamic-config.properties"));
        }
    }
    
    public String getConfigValue(String key) {
        return environment.getProperty(key);
    }
    
    public void refreshConfig() {
        // 动态刷新配置
        if (environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment configEnv = (ConfigurableEnvironment) environment;
            // 重新加载配置文件
            configEnv.getPropertySources().addFirst(new FilePropertySource("updated-config.properties"));
        }
    }
}
```

## 3. 初始化回调扩展点应用

### 3.1 @PostConstruct 应用

```java
@Service
public class UserService implements InitializingBean {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CacheManager cacheManager;
    
    @PostConstruct
    public void initialize() {
        log.info("UserService 开始初始化...");
        
        // 1. 验证依赖服务是否可用
        validateDependencies();
        
        // 2. 预热用户缓存
        warmupUserCache();
        
        // 3. 启动后台任务
        startBackgroundTasks();
        
        // 4. 注册健康检查
        registerHealthCheck();
        
        log.info("UserService 初始化完成");
    }
    
    private void validateDependencies() {
        try {
            userRepository.count();
            log.info("数据库连接正常");
        } catch (Exception e) {
            log.error("数据库连接失败", e);
            throw new RuntimeException("依赖服务不可用", e);
        }
    }
    
    private void warmupUserCache() {
        // 预热常用用户数据到缓存
        List<User> activeUsers = userRepository.findActiveUsers();
        Cache userCache = cacheManager.getCache("users");
        activeUsers.forEach(user -> userCache.put(user.getId(), user));
        log.info("预热了 {} 个用户数据到缓存", activeUsers.size());
    }
    
    private void startBackgroundTasks() {
        // 启动用户数据同步任务
        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    syncUserData();
                    Thread.sleep(300000); // 5分钟同步一次
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    private void registerHealthCheck() {
        // 注册到健康检查系统
        HealthIndicator healthIndicator = new HealthIndicator() {
            @Override
            public Health health() {
                try {
                    userRepository.count();
                    return Health.up().withDetail("userCount", userRepository.count()).build();
                } catch (Exception e) {
                    return Health.down().withException(e).build();
                }
            }
        };
        // 注册健康检查器
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("UserService 属性设置完成后的初始化...");
        
        // 执行额外的初始化逻辑
        initializeUserPermissions();
        setupEventListeners();
    }
    
    private void initializeUserPermissions() {
        // 初始化用户权限配置
        log.info("初始化用户权限配置...");
    }
    
    private void setupEventListeners() {
        // 设置事件监听器
        log.info("设置事件监听器...");
    }
}
```

### 3.2 资源预加载

```java
@Service
public class ResourcePreloaderService {
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private CacheManager cacheManager;
    
    @PostConstruct
    public void preloadResources() {
        log.info("开始预加载系统资源...");
        
        // 预加载静态资源
        preloadStaticResources();
        
        // 预加载配置数据
        preloadConfigurationData();
        
        // 预加载模板文件
        preloadTemplates();
        
        log.info("系统资源预加载完成");
    }
    
    private void preloadStaticResources() {
        // 预加载常用的静态资源到内存
        List<String> staticFiles = Arrays.asList("logo.png", "banner.jpg", "favicon.ico");
        staticFiles.forEach(file -> {
            try {
                byte[] content = fileService.readFile(file);
                cacheManager.getCache("staticResources").put(file, content);
                log.info("预加载静态资源: {}", file);
            } catch (Exception e) {
                log.warn("预加载静态资源失败: {}", file, e);
            }
        });
    }
    
    private void preloadConfigurationData() {
        // 预加载系统配置
        Map<String, Object> config = loadSystemConfig();
        cacheManager.getCache("systemConfig").put("global", config);
        log.info("预加载系统配置完成");
    }
    
    private void preloadTemplates() {
        // 预加载邮件模板、短信模板等
        List<String> templates = Arrays.asList("welcome_email", "password_reset", "order_confirmation");
        templates.forEach(template -> {
            String content = loadTemplate(template);
            cacheManager.getCache("templates").put(template, content);
            log.info("预加载模板: {}", template);
        });
    }
}
```

## 4. 销毁回调扩展点应用

### 4.1 @PreDestroy 应用

```java
@Service
public class ResourceCleanupService {
    
    private final List<AutoCloseable> resources = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    @PreDestroy
    public void cleanup() {
        log.info("开始清理系统资源...");
        
        // 1. 关闭定时任务
        shutdownScheduler();
        
        // 2. 关闭数据库连接池
        closeDatabaseConnections();
        
        // 3. 清理缓存
        clearCaches();
        
        // 4. 关闭文件句柄
        closeFileHandles();
        
        // 5. 发送关闭通知
        sendShutdownNotification();
        
        log.info("系统资源清理完成");
    }
    
    private void shutdownScheduler() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            log.info("定时任务调度器已关闭");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private void closeDatabaseConnections() {
        try {
            // 关闭数据库连接池
            DataSource dataSource = getDataSource();
            if (dataSource instanceof AutoCloseable) {
                ((AutoCloseable) dataSource).close();
            }
            log.info("数据库连接池已关闭");
        } catch (Exception e) {
            log.error("关闭数据库连接池失败", e);
        }
    }
    
    private void clearCaches() {
        try {
            // 清理所有缓存
            CacheManager cacheManager = getCacheManager();
            cacheManager.getCacheNames().forEach(cacheName -> {
                cacheManager.getCache(cacheName).clear();
            });
            log.info("缓存已清理");
        } catch (Exception e) {
            log.error("清理缓存失败", e);
        }
    }
    
    private void closeFileHandles() {
        // 关闭所有打开的文件句柄
        resources.forEach(resource -> {
            try {
                resource.close();
            } catch (Exception e) {
                log.error("关闭资源失败", e);
            }
        });
        log.info("文件句柄已关闭");
    }
    
    private void sendShutdownNotification() {
        try {
            // 发送系统关闭通知
            NotificationService notificationService = getNotificationService();
            notificationService.sendSystemNotification("系统正在关闭，请保存您的工作");
            log.info("关闭通知已发送");
        } catch (Exception e) {
            log.error("发送关闭通知失败", e);
        }
    }
}
```

## 5. 实际业务场景应用

### 5.1 微服务启动优化

```java
@Component
public class MicroserviceStartupOptimizer implements ApplicationContextAware {
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        // 微服务启动优化
        optimizeStartup(applicationContext);
    }
    
    private void optimizeStartup(ApplicationContext context) {
        // 1. 异步初始化非关键服务
        CompletableFuture.runAsync(() -> {
            initializeNonCriticalServices(context);
        });
        
        // 2. 预加载常用数据
        preloadCommonData(context);
        
        // 3. 健康检查注册
        registerHealthChecks(context);
        
        // 4. 服务发现注册
        registerServiceDiscovery(context);
    }
}
```

### 5.2 多租户系统初始化

```java
@Component
public class MultiTenantInitializer implements ApplicationContextAware {
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        // 多租户系统初始化
        initializeTenants(applicationContext);
    }
    
    private void initializeTenants(ApplicationContext context) {
        // 1. 加载租户配置
        List<TenantConfig> tenantConfigs = loadTenantConfigs();
        
        // 2. 为每个租户初始化资源
        tenantConfigs.forEach(tenant -> {
            initializeTenantResources(tenant);
        });
        
        // 3. 设置租户路由规则
        setupTenantRouting();
    }
}
```

## 6. 总结

Spring容器的生命周期扩展点在实际业务开发中非常有用，主要应用包括：

1. **性能监控** - 通过BeanPostProcessor监控Bean创建性能
2. **资源管理** - 在初始化时预加载资源，在销毁时清理资源
3. **配置管理** - 动态获取和刷新配置
4. **缓存管理** - 预热缓存，提高系统性能
5. **健康检查** - 注册系统健康检查
6. **多租户支持** - 动态切换数据源和配置
7. **微服务优化** - 异步初始化，提高启动速度
8. **监控告警** - 系统状态监控和异常告警

这些扩展点让开发者能够在Spring容器的不同生命周期阶段插入自定义逻辑，实现更灵活和强大的业务功能。
