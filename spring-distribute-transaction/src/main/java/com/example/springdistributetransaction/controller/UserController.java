package com.example.springdistributetransaction.controller;

import com.example.springdistributetransaction.entity.user.User;
import com.example.springdistributetransaction.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(request.getUsername(), request.getEmail(), request.getInitialBalance());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 查询所有用户
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        Optional<User> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据用户名查询用户
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.getUserByUsername(username);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 用户充值
     */
    @PostMapping("/{userId}/recharge")
    public ResponseEntity<String> recharge(@PathVariable Long userId, @RequestBody RechargeRequest request) {
        try {
            userService.recharge(userId, request.getAmount());
            return ResponseEntity.ok("充值成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("充值失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户余额
     */
    @GetMapping("/{userId}/balance-check")
    public ResponseEntity<Boolean> checkBalance(@PathVariable Long userId, @RequestParam BigDecimal amount) {
        boolean hasEnough = userService.hasEnoughBalance(userId, amount);
        return ResponseEntity.ok(hasEnough);
    }

    /**
     * 创建用户请求
     */
    public static class CreateUserRequest {
        private String username;
        private String email;
        private BigDecimal initialBalance;

        // Getters and Setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public BigDecimal getInitialBalance() {
            return initialBalance;
        }

        public void setInitialBalance(BigDecimal initialBalance) {
            this.initialBalance = initialBalance;
        }
    }

    /**
     * 充值请求
     */
    public static class RechargeRequest {
        private BigDecimal amount;

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}