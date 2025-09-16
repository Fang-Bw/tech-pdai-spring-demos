package com.example.springdistributetransaction.service;

import com.example.springdistributetransaction.entity.user.User;
import com.example.springdistributetransaction.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * 创建用户
     */
    @Transactional
    public User createUser(String username, String email, BigDecimal initialBalance) {
        log.info("创建用户 - 用户名: {}, 邮箱: {}, 初始余额: {}", username, email, initialBalance);

        // 检查用户名是否已存在
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已存在: " + username);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setBalance(initialBalance != null ? initialBalance : BigDecimal.ZERO);

        User savedUser = userRepository.save(user);
        log.info("成功创建用户: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * 根据ID查询用户
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * 根据用户名查询用户
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 查询所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 充值
     */
    @Transactional
    public void recharge(Long userId, BigDecimal amount) {
        log.info("用户充值 - 用户ID: {}, 金额: {}", userId, amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }

        int updatedRows = userRepository.addBalance(userId, amount);
        if (updatedRows == 0) {
            throw new RuntimeException("用户不存在: " + userId);
        }

        log.info("充值成功 - 用户ID: {}, 金额: {}", userId, amount);
    }

    /**
     * 检查用户余额
     */
    public boolean hasEnoughBalance(Long userId, BigDecimal amount) {
        return userRepository.hasEnoughBalance(userId, amount);
    }
}