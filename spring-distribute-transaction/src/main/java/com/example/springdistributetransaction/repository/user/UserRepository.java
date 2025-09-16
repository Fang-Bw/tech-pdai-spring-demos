package com.example.springdistributetransaction.repository.user;

import com.example.springdistributetransaction.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 更新用户余额
     */
    @Modifying
    @Query("UPDATE User u SET u.balance = u.balance - :amount WHERE u.id = :userId AND u.balance >= :amount")
    int deductBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
    
    /**
     * 增加用户余额
     */
    @Modifying
    @Query("UPDATE User u SET u.balance = u.balance + :amount WHERE u.id = :userId")
    int addBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
    
    /**
     * 检查用户余额是否足够
     */
    @Query("SELECT CASE WHEN u.balance >= :amount THEN true ELSE false END FROM User u WHERE u.id = :userId")
    boolean hasEnoughBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}