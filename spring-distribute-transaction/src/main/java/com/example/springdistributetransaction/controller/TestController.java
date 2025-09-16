package com.example.springdistributetransaction.controller;

import com.example.springdistributetransaction.entity.order.Order;
import com.example.springdistributetransaction.entity.user.User;
import com.example.springdistributetransaction.service.OrderService;
import com.example.springdistributetransaction.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试控制器
 * 提供分布式事务演示和测试接口
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final UserService userService;
    private final OrderService orderService;

    /**
     * 初始化测试数据
     */
    @PostMapping("/init-data")
    public ResponseEntity<Map<String, Object>> initTestData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 创建测试用户
            User user1 = userService.createUser("testuser1", "test1@example.com", new BigDecimal("1000.00"));
            User user2 = userService.createUser("testuser2", "test2@example.com", new BigDecimal("500.00"));
            
            result.put("success", true);
            result.put("message", "测试数据初始化成功");
            result.put("users", List.of(user1, user2));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "测试数据初始化失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 测试分布式事务 - 正常流程
     */
    @PostMapping("/transaction-success")
    public ResponseEntity<Map<String, Object>> testTransactionSuccess() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找测试用户
            User user = userService.getUserByUsername("testuser1").orElse(null);
            if (user == null) {
                result.put("success", false);
                result.put("message", "测试用户不存在，请先初始化测试数据");
                return ResponseEntity.badRequest().body(result);
            }
            
            BigDecimal beforeBalance = user.getBalance();
            
            // 创建订单（会扣减用户余额）
            Order order = orderService.createOrder(user.getId(), "测试商品", new BigDecimal("100.00"), 2);
            
            // 查询用户最新余额
            User updatedUser = userService.getUserById(user.getId()).orElse(null);
            BigDecimal afterBalance = updatedUser != null ? updatedUser.getBalance() : BigDecimal.ZERO;
            
            result.put("success", true);
            result.put("message", "分布式事务执行成功");
            result.put("order", order);
            result.put("beforeBalance", beforeBalance);
            result.put("afterBalance", afterBalance);
            result.put("deductedAmount", beforeBalance.subtract(afterBalance));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "分布式事务执行失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 测试分布式事务 - 异常回滚
     */
    @PostMapping("/transaction-rollback")
    public ResponseEntity<Map<String, Object>> testTransactionRollback() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找测试用户
            User user = userService.getUserByUsername("testuser1").orElse(null);
            if (user == null) {
                result.put("success", false);
                result.put("message", "测试用户不存在，请先初始化测试数据");
                return ResponseEntity.badRequest().body(result);
            }
            
            BigDecimal beforeBalance = user.getBalance();
            
            // 尝试创建订单（会抛出异常并回滚）
            try {
                orderService.createOrderWithException(user.getId(), "测试商品", new BigDecimal("50.00"), 1);
            } catch (Exception e) {
                // 预期的异常，忽略
            }
            
            // 查询用户最新余额
            User updatedUser = userService.getUserById(user.getId()).orElse(null);
            BigDecimal afterBalance = updatedUser != null ? updatedUser.getBalance() : BigDecimal.ZERO;
            
            result.put("success", true);
            result.put("message", "分布式事务回滚测试完成");
            result.put("beforeBalance", beforeBalance);
            result.put("afterBalance", afterBalance);
            result.put("isRolledBack", beforeBalance.equals(afterBalance));
            result.put("explanation", "如果余额相等，说明事务成功回滚");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "分布式事务回滚测试失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 查看当前状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<User> users = userService.getAllUsers();
            List<Order> orders = orderService.getAllOrders();
            
            result.put("success", true);
            result.put("users", users);
            result.put("orders", orders);
            result.put("userCount", users.size());
            result.put("orderCount", orders.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 清理测试数据
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanup() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "请手动清理数据库中的测试数据");
        return ResponseEntity.ok(result);
    }
}