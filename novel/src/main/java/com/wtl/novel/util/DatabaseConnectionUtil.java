package com.wtl.novel.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionUtil {

    private static volatile HikariDataSource primaryDataSource;
    private static volatile HikariDataSource secondaryDataSource;
    private static final Object PRIMARY_LOCK = new Object();
    private static final Object SECONDARY_LOCK = new Object();

    private DatabaseConnectionUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 获取主数据库连接
     * 使用连接池管理,自动处理连接复用
     */
    public static Connection getPrimaryConnection() throws SQLException {
        return getPrimaryDataSource().getConnection();
    }

    /**
     * 获取从数据库连接
     * 使用连接池管理,自动处理连接复用
     */
    public static Connection getSecondaryConnection() throws SQLException {
        return getSecondaryDataSource().getConnection();
    }

    /**
     * 获取连接 (兼容)
     */
    public static Connection getConnection() throws SQLException {
        return getPrimaryConnection();
    }

    /**
     * 获取主数据库连接池
     */
    private static DataSource getPrimaryDataSource() {
        if (primaryDataSource == null) {
            synchronized (PRIMARY_LOCK) {
                if (primaryDataSource == null) {
                    String dbUrl = System.getenv("PRIMARY_DB_URL");
                    String dbUser = System.getenv("PRIMARY_DB_USERNAME");
                    String dbPassword = System.getenv("PRIMARY_DB_PASSWORD");

                    if (dbUrl == null || dbUrl.isEmpty()) {
                        dbUrl = "jdbc:mariadb://localhost:3306/novel?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
                    }
                    if (dbUser == null) {
                        dbUser = "";
                    }
                    if (dbPassword == null) {
                        dbPassword = "";
                    }

                    primaryDataSource = createDataSource(dbUrl, dbUser, dbPassword, "PrimaryPool");
                }
            }
        }
        return primaryDataSource;
    }

    /**
     * 获取从数据库连接池
     */
    private static DataSource getSecondaryDataSource() {
        if (secondaryDataSource == null) {
            synchronized (SECONDARY_LOCK) {
                if (secondaryDataSource == null) {
                    String dbUrl = System.getenv("SECONDARY_DB_URL");
                    String dbUser = System.getenv("SECONDARY_DB_USERNAME");
                    String dbPassword = System.getenv("SECONDARY_DB_PASSWORD");

                    if (dbUrl == null || dbUrl.isEmpty()) {
                        dbUrl = "jdbc:mariadb://localhost:3306/novel?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
                    }
                    if (dbUser == null) {
                        dbUser = "";
                    }
                    if (dbPassword == null) {
                        dbPassword = "";
                    }

                    secondaryDataSource = createDataSource(dbUrl, dbUser, dbPassword, "SecondaryPool");
                }
            }
        }
        return secondaryDataSource;
    }

    private static HikariDataSource createDataSource(String jdbcUrl, String username, String password, String poolName) {
        HikariConfig config = new HikariConfig();
        
        // 基本连接配置
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName(poolName);
        
        // 连接池大小配置
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        
        // 连接超时配置
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        // 连接测试配置
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000); 
        
        // 性能优化配置
        config.setAutoCommit(true);
        config.setReadOnly(false);
        
        // MariaDB 驱动类
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        
        // 连接池健康检查
        config.setLeakDetectionThreshold(60000);
        
        return new HikariDataSource(config);
    }

    /**
     * 关闭连接池
     */
    public static void shutdown() {
        if (primaryDataSource != null && !primaryDataSource.isClosed()) {
            primaryDataSource.close();
        }
        if (secondaryDataSource != null && !secondaryDataSource.isClosed()) {
            secondaryDataSource.close();
        }
    }
}
