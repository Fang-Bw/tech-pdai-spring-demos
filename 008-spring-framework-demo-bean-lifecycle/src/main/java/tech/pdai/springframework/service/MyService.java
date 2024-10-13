package tech.pdai.springframework.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class MyService {

    @Value("${db.url}")
    private String dbUrl;

    @Value("${db.username}")
    private String dbUsername;

    @Value("${db.password}")
    private String dbPassword;

    private Connection connection;

    @PostConstruct
    public void init() {
        // 校验配置文件中的属性
        if (dbUrl == null || dbUsername == null || dbPassword == null) {
            throw new IllegalArgumentException("Database properties must be set");
        }

        // 打开数据库连接
        try {
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            System.out.println("Database connection established");
            // 预加载数据
            preloadData();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database", e);
        }
    }

    private void preloadData() {
        // 实现数据预加载逻辑
        System.out.println("Preloading data...");
        // 例如执行查询并填充缓存等
    }

    @PreDestroy
    public void cleanup() {
        // 关闭数据库连接
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
