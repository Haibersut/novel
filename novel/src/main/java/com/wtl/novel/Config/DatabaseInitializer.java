package com.wtl.novel.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库初始化器
 * 在应用启动时检查必要的表是否存在，如果不存在则创建
 */
@Component
@Order(1)
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final DataSource primaryDataSource;
    private final DataSource secondaryDataSource;

    /**
     * 构造函数注入数据源
     * @param primaryDataSource 主数据源
     * @param secondaryDataSource 从数据源
     */
    public DatabaseInitializer(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("secondaryDataSource") DataSource secondaryDataSource) {
        this.primaryDataSource = primaryDataSource;
        this.secondaryDataSource = secondaryDataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("开始数据库初始化检查...");
        
        // 初始化主数据库
        initializePrimaryDatabase();
        
        // 初始化从数据库（如果配置了双数据库模式）
        if (secondaryDataSource != null) {
            initializeSecondaryDatabase();
        }
        
        log.info("数据库初始化检查完成");
    }

    /**
     * 初始化主数据库
     */
    private void initializePrimaryDatabase() {
        try (Connection conn = primaryDataSource.getConnection()) {
            log.info("检查主数据库表结构...");
            
            List<String> missingTables = new ArrayList<>();
            
            // 检查所有必需的表
            String[] requiredTables = {
                "chapter", "chapter_comment", "chapter_error_execute", "chapter_execute",
                "chapter_image_links", "chapter_sync", "chapter_updates", "comment",
                "credential", "dictionary", "favorites", "favorite_groups",
                "invitation_code", "message", "notes", "novel", "novel_chapter",
                "novel_download_limit", "novel_tag", "platform", "platform_api_key",
                "post", "post_agree", "post_comment", "reading_record", "request_log",
                "tag", "terminology", "user", "user_access_logs", "user_blacklist",
                "user_chapter_edit", "user_feedback", "user_glossary",
                "user_novel_relation", "user_operation_log", "user_tag_filter"
            };
            
            for (String tableName : requiredTables) {
                if (!tableExists(conn, tableName)) {
                    missingTables.add(tableName);
                }
            }
            
            if (missingTables.isEmpty()) {
                log.info("主数据库所有表已存在，跳过创建");
            } else {
                log.warn("主数据库缺少以下表: {}", missingTables);
                createPrimaryTables(conn, missingTables);
            }
            
        } catch (SQLException e) {
            log.error("初始化主数据库失败: {}", e.getMessage(), e);
            throw new DatabaseInitializationException("主数据库初始化失败", e);
        }
    }

    /**
     * 初始化从数据库（仅包含 chapter_scaling_up_one 表）
     */
    private void initializeSecondaryDatabase() {
        try (Connection conn = secondaryDataSource.getConnection()) {
            log.info("检查从数据库表结构...");
            
            String tableName = "chapter_scaling_up_one";
            
            if (!tableExists(conn, tableName)) {
                log.info("从数据库表 {} 不存在，开始创建...", tableName);
                createSecondaryTable(conn);
                log.info("从数据库表 {} 创建成功", tableName);
            } else {
                log.info("从数据库表 {} 已存在，跳过创建", tableName);
            }
            
        } catch (SQLException e) {
            log.error("初始化从数据库失败: {}", e.getMessage(), e);
            throw new DatabaseInitializationException("从数据库初始化失败", e);
        }
    }

    /**
     * 检查表是否存在
     */
    private boolean tableExists(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    /**
     * 创建主数据库表
     */
    private void createPrimaryTables(Connection conn, List<String> missingTables) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 禁用外键检查以避免表创建顺序问题
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            for (String tableName : missingTables) {
                log.info("创建主数据库表: {}", tableName);
                String createTableSql = TableSqlRepository.getCreateTableSql(tableName);
                if (createTableSql != null) {
                    stmt.execute(createTableSql);
                    log.info("表 {} 创建成功", tableName);
                } else {
                    log.warn("未找到表 {} 的创建语句", tableName);
                }
            }
            
            // 重新启用外键检查
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            
            log.info("主数据库表创建完成");
        }
    }

    /**
     * 创建从数据库表（chapter_scaling_up_one）
     */
    private void createSecondaryTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE `chapter_scaling_up_one` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `novel_id` BIGINT(20) NULL DEFAULT NULL,
                `title` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `chapter_number` INT(11) NOT NULL,
                `content` LONGBLOB NULL DEFAULT NULL COMMENT '压缩后的二进制内容',
                `is_deleted` TINYINT(1) NULL DEFAULT '0',
                `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP(),
                `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
                `own_photo` TINYINT(1) NULL DEFAULT '0',
                `true_id` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `idx_chapter_novel_id_title` (`novel_id`, `title`) USING BTREE,
                INDEX `idx_novel_chapter_num` (`novel_id`, `chapter_number`) USING BTREE,
                INDEX `idx_true_id` (`true_id`) USING BTREE,
                INDEX `idx_deleted_updated` (`is_deleted`, `updated_at` DESC) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """;
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}
