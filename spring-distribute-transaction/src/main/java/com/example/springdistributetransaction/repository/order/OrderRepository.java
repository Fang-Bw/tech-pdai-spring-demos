package com.example.springdistributetransaction.repository.order;

import com.example.springdistributetransaction.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单数据访问层
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 根据用户ID查找订单
     */
    List<Order> findByUserId(Long userId);
    
    /**
     * 根据用户ID和订单状态查找订单
     */
    List<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status);
    
    /**
     * 统计用户的订单数量
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * 根据状态查找订单
     */
    List<Order> findByStatus(Order.OrderStatus status);
}