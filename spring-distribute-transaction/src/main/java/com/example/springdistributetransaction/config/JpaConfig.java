package com.example.springdistributetransaction.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManagerFactory;

/**
 * JPA配置类
 * 配置不同数据源的Repository扫描
 */
@Configuration
public class JpaConfig {

    /**
     * 用户数据源的JPA配置
     */
    @Configuration
    @EnableJpaRepositories(
            basePackages = "com.example.springdistributetransaction.repository.user",
            entityManagerFactoryRef = "userEntityManagerFactory",
            transactionManagerRef = "transactionManager"
    )
    static class UserJpaConfig {
    }

    /**
     * 订单数据源的JPA配置
     */
    @Configuration
    @EnableJpaRepositories(
            basePackages = "com.example.springdistributetransaction.repository.order",
            entityManagerFactoryRef = "orderEntityManagerFactory",
            transactionManagerRef = "transactionManager"
    )
    static class OrderJpaConfig {
    }
}