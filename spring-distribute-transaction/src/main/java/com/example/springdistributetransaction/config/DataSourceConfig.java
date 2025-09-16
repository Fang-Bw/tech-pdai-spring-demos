package com.example.springdistributetransaction.config;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import java.util.Properties;

/**
 * 多数据源配置类
 * 配置两个数据源用于演示分布式事务
 */
@Configuration
public class DataSourceConfig {

    /**
     * 第一个数据源 - 用户数据库
     */
    @Bean(name = "userDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.user")
    public DataSource userDataSource() {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setUniqueResourceName("userDataSource");
        dataSource.setXaDataSourceClassName("com.mysql.cj.jdbc.MysqlXADataSource");
        
        Properties properties = new Properties();
        properties.setProperty("url", "jdbc:mysql://localhost:3306/user_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        properties.setProperty("user", "root");
        properties.setProperty("password", "123456");
        dataSource.setXaProperties(properties);
        
        dataSource.setMinPoolSize(5);
        dataSource.setMaxPoolSize(20);
        dataSource.setMaxLifetime(20000);
        dataSource.setBorrowConnectionTimeout(10000);
        dataSource.setLoginTimeout(10000);
        dataSource.setMaintenanceInterval(60);
        dataSource.setMaxIdleTime(60);
        
        return dataSource;
    }

    /**
     * 第二个数据源 - 订单数据库
     */
    @Bean(name = "orderDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.order")
    public DataSource orderDataSource() {
        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setUniqueResourceName("orderDataSource");
        dataSource.setXaDataSourceClassName("com.mysql.cj.jdbc.MysqlXADataSource");
        
        Properties properties = new Properties();
        properties.setProperty("url", "jdbc:mysql://localhost:3306/order_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        properties.setProperty("user", "root");
        properties.setProperty("password", "123456");
        dataSource.setXaProperties(properties);
        
        dataSource.setMinPoolSize(5);
        dataSource.setMaxPoolSize(20);
        dataSource.setMaxLifetime(20000);
        dataSource.setBorrowConnectionTimeout(10000);
        dataSource.setLoginTimeout(10000);
        dataSource.setMaintenanceInterval(60);
        dataSource.setMaxIdleTime(60);
        
        return dataSource;
    }

    /**
     * JTA事务管理器
     */
    @Bean(name = "userTransaction")
    public UserTransaction userTransaction() throws Throwable {
        UserTransactionImp userTransactionImp = new UserTransactionImp();
        userTransactionImp.setTransactionTimeout(10000);
        return userTransactionImp;
    }

    @Bean(name = "atomikosTransactionManager")
    public UserTransactionManager atomikosTransactionManager() throws Throwable {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(false);
        return userTransactionManager;
    }

    @Bean(name = "transactionManager")
    @Primary
    public JtaTransactionManager transactionManager() throws Throwable {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(atomikosTransactionManager());
        jtaTransactionManager.setUserTransaction(userTransaction());
        return jtaTransactionManager;
    }

    /**
     * 用户数据源的EntityManagerFactory
     */
    @Bean(name = "userEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean userEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(userDataSource());
        factory.setPackagesToScan("com.example.springdistributetransaction.entity.user");
        factory.setPersistenceUnitName("userPersistenceUnit");
        
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        factory.setJpaVendorAdapter(vendorAdapter);
        
        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        jpaProperties.setProperty("hibernate.show_sql", "true");
        jpaProperties.setProperty("hibernate.format_sql", "true");
        jpaProperties.setProperty("hibernate.transaction.jta.platform", "com.atomikos.icatch.jta.hibernate4.AtomikosPlatform");
        jpaProperties.setProperty("javax.persistence.transactionType", "JTA");
        factory.setJpaProperties(jpaProperties);
        
        return factory;
    }

    /**
     * 订单数据源的EntityManagerFactory
     */
    @Bean(name = "orderEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean orderEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(orderDataSource());
        factory.setPackagesToScan("com.example.springdistributetransaction.entity.order");
        factory.setPersistenceUnitName("orderPersistenceUnit");
        
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        factory.setJpaVendorAdapter(vendorAdapter);
        
        Properties jpaProperties = new Properties();
        jpaProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        jpaProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        jpaProperties.setProperty("hibernate.show_sql", "true");
        jpaProperties.setProperty("hibernate.format_sql", "true");
        jpaProperties.setProperty("hibernate.transaction.jta.platform", "com.atomikos.icatch.jta.hibernate4.AtomikosPlatform");
        jpaProperties.setProperty("javax.persistence.transactionType", "JTA");
        factory.setJpaProperties(jpaProperties);
        
        return factory;
    }
}