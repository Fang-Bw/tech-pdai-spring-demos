package com.example.springdistributetransaction.controller;

import com.example.springdistributetransaction.entity.order.Order;
import com.example.springdistributetransaction.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 订单控制器
 * 提供分布式事务测试接口
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单 - 正常流程（分布式事务）
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(
                    request.getUserId(),
                    request.getProductName(),
                    request.getAmount(),
                    request.getQuantity()
            );
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 创建订单 - 模拟异常回滚（分布式事务测试）
     */
    @PostMapping("/with-exception")
    public ResponseEntity<String> createOrderWithException(@RequestBody CreateOrderRequest request) {
        try {
            orderService.createOrderWithException(
                    request.getUserId(),
                    request.getProductName(),
                    request.getAmount(),
                    request.getQuantity()
            );
            return ResponseEntity.ok("订单创建成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("订单创建失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok("订单取消成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("订单取消失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有订单
     */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * 根据ID查询订单
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);
        return order.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 查询用户订单
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * 创建订单请求
     */
    public static class CreateOrderRequest {
        private Long userId;
        private String productName;
        private BigDecimal amount;
        private Integer quantity;

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}