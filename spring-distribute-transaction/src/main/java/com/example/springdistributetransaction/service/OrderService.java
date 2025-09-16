package com.example.springdistributetransaction.service;

import com.example.springdistributetransaction.entity.order.Order;
import com.example.springdistributetransaction.entity.user.User;
import com.example.springdistributetransaction.repository.order.OrderRepository;
import com.example.springdistributetransaction.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 订单服务类
 * 演示分布式事务的核心业务逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    /**
     * 创建订单 - 分布式事务示例
     * 该方法会同时操作用户数据库和订单数据库
     * 使用JTA事务管理器确保数据一致性
     */
    @Transactional
    public Order createOrder(Long userId, String productName, BigDecimal amount, Integer quantity) {
        log.info("开始创建订单 - 用户ID: {}, 商品: {}, 金额: {}, 数量: {}", userId, productName, amount, quantity);

        // 1. 检查用户是否存在
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("用户不存在: " + userId);
        }

        User user = userOptional.get();
        BigDecimal totalAmount = amount.multiply(BigDecimal.valueOf(quantity));

        // 2. 检查用户余额是否足够
        if (!userRepository.hasEnoughBalance(userId, totalAmount)) {
            throw new RuntimeException("用户余额不足，当前余额: " + user.getBalance() + ", 需要金额: " + totalAmount);
        }

        // 3. 扣减用户余额（操作用户数据库）
        int updatedRows = userRepository.deductBalance(userId, totalAmount);
        if (updatedRows == 0) {
            throw new RuntimeException("扣减用户余额失败，可能余额不足或用户不存在");
        }
        log.info("成功扣减用户余额: {} 元", totalAmount);

        // 4. 创建订单（操作订单数据库）
        Order order = new Order();
        order.setUserId(userId);
        order.setProductName(productName);
        order.setAmount(amount);
        order.setQuantity(quantity);
        order.setStatus(Order.OrderStatus.COMPLETED);

        Order savedOrder = orderRepository.save(order);
        log.info("成功创建订单: {}", savedOrder.getId());

        return savedOrder;
    }

    /**
     * 创建订单 - 模拟异常回滚
     * 该方法故意在最后抛出异常，用于测试分布式事务回滚
     */
    @Transactional
    public Order createOrderWithException(Long userId, String productName, BigDecimal amount, Integer quantity) {
        log.info("开始创建订单（模拟异常） - 用户ID: {}, 商品: {}, 金额: {}, 数量: {}", userId, productName, amount, quantity);

        // 1. 检查用户是否存在
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new RuntimeException("用户不存在: " + userId);
        }

        BigDecimal totalAmount = amount.multiply(BigDecimal.valueOf(quantity));

        // 2. 检查用户余额是否足够
        if (!userRepository.hasEnoughBalance(userId, totalAmount)) {
            throw new RuntimeException("用户余额不足");
        }

        // 3. 扣减用户余额
        int updatedRows = userRepository.deductBalance(userId, totalAmount);
        if (updatedRows == 0) {
            throw new RuntimeException("扣减用户余额失败");
        }
        log.info("成功扣减用户余额: {} 元", totalAmount);

        // 4. 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setProductName(productName);
        order.setAmount(amount);
        order.setQuantity(quantity);
        order.setStatus(Order.OrderStatus.COMPLETED);

        Order savedOrder = orderRepository.save(order);
        log.info("成功创建订单: {}", savedOrder.getId());

        // 5. 模拟异常 - 这将导致整个事务回滚
        throw new RuntimeException("模拟业务异常，测试分布式事务回滚");
    }

    /**
     * 取消订单 - 退还用户余额
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        log.info("开始取消订单: {}", orderId);

        // 1. 查找订单
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (!orderOptional.isPresent()) {
            throw new RuntimeException("订单不存在: " + orderId);
        }

        Order order = orderOptional.get();
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("订单已经被取消");
        }

        // 2. 计算退款金额
        BigDecimal refundAmount = order.getAmount().multiply(BigDecimal.valueOf(order.getQuantity()));

        // 3. 退还用户余额
        userRepository.addBalance(order.getUserId(), refundAmount);
        log.info("成功退还用户余额: {} 元", refundAmount);

        // 4. 更新订单状态
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("成功取消订单: {}", orderId);
    }

    /**
     * 查询用户订单
     */
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    /**
     * 查询所有订单
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * 根据ID查询订单
     */
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }
}