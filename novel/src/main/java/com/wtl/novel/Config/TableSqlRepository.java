package com.wtl.novel.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库表SQL语句仓库
 * 包含所有表的CREATE TABLE语句
 */
public class TableSqlRepository {

    private static final Map<String, String> TABLE_SQL_MAP = new HashMap<>();

    // 私有构造函数，防止实例化
    private TableSqlRepository() {
        throw new UnsupportedOperationException("Utility class");
    }

    static {
        initializeTableSql();
    }

    /**
     * 获取指定表的创建SQL语句
     * @param tableName 表名
     * @return SQL语句，如果表不存在则返回null
     */
    public static String getCreateTableSql(String tableName) {
        return TABLE_SQL_MAP.get(tableName);
    }

    /**
     * 初始化所有表的SQL语句
     */
    private static void initializeTableSql() {
        TABLE_SQL_MAP.put("chapter", """
            CREATE TABLE `chapter` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `novel_id` BIGINT(20) NULL DEFAULT NULL,
                `title` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `chapter_number` INT(11) NOT NULL,
                `content` LONGBLOB NULL DEFAULT NULL COMMENT '压缩后的二进制内容',
                `is_deleted` TINYINT(1) NULL DEFAULT '0',
                `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP(),
                `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
                `own_photo` TINYINT(1) NULL DEFAULT '0',
                `true_id` VARCHAR(64) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
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
            """);

        TABLE_SQL_MAP.put("chapter_comment", """
            CREATE TABLE `chapter_comment` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `novel_id` BIGINT(20) NOT NULL,
                `username` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `avatar` VARCHAR(255) NULL DEFAULT 'https://via.placeholder.com/40' COLLATE 'utf8mb4_0900_ai_ci',
                `content` TEXT NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `likes` INT(11) NULL DEFAULT '0',
                `parent_id` BIGINT(20) NULL DEFAULT NULL,
                `reply_to` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `created_at` LONGBLOB NULL DEFAULT NULL,
                `user_id` BIGINT(20) NOT NULL,
                `chapter_id` BIGINT(20) NOT NULL,
                `text_num` INT(11) NOT NULL,
                PRIMARY KEY (`id`) USING BTREE,
                INDEX `idx_chapter_text` (`chapter_id`, `text_num`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("chapter_error_execute", """
            CREATE TABLE `chapter_error_execute` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `novel_id` BIGINT(20) NULL DEFAULT NULL,
                `title` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `chapter_number` INT(11) NOT NULL,
                `content` MEDIUMTEXT NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `translator_content` MEDIUMTEXT NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `is_deleted` TINYINT(1) NULL DEFAULT '0',
                `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP(),
                `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
                `now_state` INT(11) NOT NULL,
                `now_photo` TINYINT(1) NULL DEFAULT '0',
                `true_id` VARCHAR(64) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `idx_chapter_error_execute_novel_id_title` (`novel_id`, `title`) USING BTREE,
                INDEX `idx_novel_id` (`novel_id`) USING BTREE,
                INDEX `idx_now_state` (`now_state`) USING BTREE,
                INDEX `idx_state_updated` (`now_state`, `updated_at` DESC) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("chapter_execute", """
            CREATE TABLE `chapter_execute` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `novel_id` BIGINT(20) NULL DEFAULT NULL,
                `title` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `chapter_number` INT(11) NOT NULL,
                `content` MEDIUMTEXT NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `translator_content` MEDIUMTEXT NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `is_deleted` TINYINT(1) NULL DEFAULT '0',
                `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP(),
                `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
                `now_state` INT(11) NOT NULL,
                `own_photo` TINYINT(1) NULL DEFAULT '0',
                `true_id` VARCHAR(64) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `idx_chapter_execute_novel_id_title` (`novel_id`, `title`) USING BTREE,
                INDEX `idx_novel_id` (`novel_id`) USING BTREE,
                INDEX `idx_now_state` (`now_state`) USING BTREE,
                INDEX `idx_state_updated` (`now_state`, `updated_at` DESC) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("chapter_image_links", """
            CREATE TABLE `chapter_image_links` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `chapter_true_id` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `content_link` VARCHAR(512) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `location` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `cf` TINYINT(1) NULL DEFAULT '0',
                PRIMARY KEY (`id`) USING BTREE,
                INDEX `idx_chapter_true_id` (`chapter_true_id`) USING BTREE,
                INDEX `idx_chapter_cf` (`chapter_true_id`, `cf`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("chapter_sync", """
            CREATE TABLE `chapter_sync` (
                `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                `chapter_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '章节ID',
                `host_server_name` VARCHAR(128) NOT NULL COMMENT '目标主机/服务器名称' COLLATE 'utf8mb4_0900_ai_ci',
                `is_synced` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '是否已经同步标识：0-未同步；1-已同步',
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `uk_chapter_sync_chapter_id` (`chapter_id`) USING BTREE,
                INDEX `idx_chapter_sync_chapter_id_synced` (`chapter_id`, `is_synced`) USING BTREE
            )
            COMMENT='章节同步状态表'
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("chapter_updates", """
            CREATE TABLE `chapter_updates` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `chapter_id` BIGINT(20) NOT NULL,
                `content` TEXT NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP(),
                `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
                `user_id` BIGINT(20) NOT NULL,
                PRIMARY KEY (`id`) USING BTREE,
                INDEX `idx_chapter_id` (`chapter_id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("comment", """
            CREATE TABLE `comment` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `chapter_id` BIGINT(20) NOT NULL,
                `user_id` BIGINT(20) NOT NULL,
                `content` TEXT NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
                `is_deleted` TINYINT(1) NOT NULL DEFAULT '0',
                PRIMARY KEY (`id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("credential", """
            CREATE TABLE `credential` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `user_id` BIGINT(20) NOT NULL,
                `token` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP(),
                `expired_at` TIMESTAMP NULL DEFAULT NULL,
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `token` (`token`) USING BTREE,
                INDEX `user_id` (`user_id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("dictionary", """
            CREATE TABLE `dictionary` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `key_field` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `value_field` TEXT NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `description` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `is_deleted` TINYINT(1) NULL DEFAULT '0',
                `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP(),
                `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `key_field` (`key_field`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("favorites", """
            CREATE TABLE `favorites` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `object_name` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `user_id` BIGINT(20) NOT NULL,
                `favorite_type` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `object_id` BIGINT(20) NOT NULL,
                `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
                `group_id` BIGINT(20) NULL DEFAULT '0',
                PRIMARY KEY (`id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("favorite_groups", """
            CREATE TABLE `favorite_groups` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `user_id` BIGINT(20) NOT NULL,
                `name` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
                PRIMARY KEY (`id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("invitation_code", """
            CREATE TABLE `invitation_code` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `code` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `used` TINYINT(1) NULL DEFAULT '0',
                `bound_email` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP(),
                `user_id` BIGINT(20) NULL DEFAULT NULL,
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `code` (`code`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("message", """
            CREATE TABLE `message` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP() COMMENT '创建时间',
                `user_id` BIGINT(20) NOT NULL COMMENT '接收消息的用户ID',
                `execute_user_id` BIGINT(20) NOT NULL COMMENT '触发消息的用户ID',
                `message_type` TINYINT(4) NOT NULL COMMENT '消息类型（1=点赞，2=评论，3=关注，4=系统通知等）',
                `message_content` TEXT NULL DEFAULT NULL COMMENT '消息内容（可选，部分类型可能需要自定义文案）' COLLATE 'utf8mb4_0900_ai_ci',
                `post_id` BIGINT(20) NULL DEFAULT NULL COMMENT '关联的帖子ID（可选）',
                `post_comment_id` BIGINT(20) NULL DEFAULT NULL COMMENT '关联的评论ID（可选）',
                `is_read` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '是否已读（0=未读，1=已读）',
                `username` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `text_num` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                PRIMARY KEY (`id`) USING BTREE,
                INDEX `idx_user_read_time` (`user_id`, `is_read`, `create_time` DESC) USING BTREE,
                INDEX `idx_read_batch` (`user_id`, `is_read`) USING BTREE,
                INDEX `idx_user_read_type_time` (`user_id`, `is_read`, `message_type`, `create_time` DESC) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        addRemainingTables();
    }

    private static void addRemainingTables() {
        TABLE_SQL_MAP.put("notes", """
            CREATE TABLE `notes` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `user_id` BIGINT(20) NOT NULL,
                `novel_id` BIGINT(20) NOT NULL,
                `chapter_id` BIGINT(20) NOT NULL,
                `content` TEXT NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `is_deleted` TINYINT(1) NOT NULL DEFAULT '0',
                `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
                PRIMARY KEY (`id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("novel", """
            CREATE TABLE `novel` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `title` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `up` BIGINT(20) NOT NULL DEFAULT '0',
                `is_deleted` TINYINT(1) NULL DEFAULT '0',
                `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP(),
                `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
                `photo_url` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `true_name` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `true_id` VARCHAR(128) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `platform` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `plat_form` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `novel_type` VARCHAR(64) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `font_number` MEDIUMTEXT NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `spans` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `novel_read` BIGINT(20) NOT NULL DEFAULT '0',
                `novel_like` BIGINT(20) NOT NULL DEFAULT '0',
                `recommend` BIGINT(20) NOT NULL DEFAULT '0',
                `author_id` VARCHAR(255) NOT NULL DEFAULT '' COLLATE 'utf8mb4_0900_ai_ci',
                `author_name` VARCHAR(255) NOT NULL DEFAULT '' COLLATE 'utf8mb4_0900_ai_ci',
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `unique_true_id` (`true_id`) USING BTREE,
                INDEX `idx_novel_platform_type_font` (`platform`, `novel_type`, `font_number`(255)) USING BTREE,
                INDEX `idx_created_at` (`created_at`) USING BTREE,
                INDEX `idx_updated_at` (`updated_at`) USING BTREE,
                INDEX `idx_author_id` (`author_id`) USING BTREE,
                INDEX `idx_platform_trueName` (`platform`, `true_name`) USING BTREE,
                INDEX `idx_platform_title` (`platform`, `title`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        addNovelRelatedTables();
        addUserRelatedTables();
        addPostRelatedTables();
        addMiscTables();
    }

    private static void addNovelRelatedTables() {
        TABLE_SQL_MAP.put("novel_chapter", """
            CREATE TABLE `novel_chapter` (
                `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
                `novel_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '小说ID',
                `chapter_num` INT(10) UNSIGNED NOT NULL COMMENT '章节序号（第几章）',
                `chapter_true_id` VARCHAR(64) NOT NULL COMMENT '章节真实业务ID（如第三方回传ID）' COLLATE 'utf8mb4_0900_ai_ci',
                `platform_id` INT(10) UNSIGNED NOT NULL COMMENT '平台ID（1=起点，2=纵横…）',
                `downloaded` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否已下载（0=未下载，1=已下载）',
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `uk_novel_chapter_num` (`novel_id`, `chapter_num`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("novel_download_limit", """
            CREATE TABLE `novel_download_limit` (
                `novel_id` BIGINT(20) NOT NULL,
                `last_download_time` DATETIME NULL DEFAULT NULL,
                PRIMARY KEY (`novel_id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            """);

        TABLE_SQL_MAP.put("novel_tag", """
            CREATE TABLE `novel_tag` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `novel_id` BIGINT(20) NOT NULL,
                `tag_id` BIGINT(20) NOT NULL,
                PRIMARY KEY (`id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);
    }

    private static void addUserRelatedTables() {
        TABLE_SQL_MAP.put("user", """
            CREATE TABLE `user` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `email` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `password` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `invitation_code_id` BIGINT(20) NULL DEFAULT NULL,
                `point` BIGINT(20) NULL DEFAULT '0',
                `upload` TINYINT(1) NULL DEFAULT '0',
                `hide_read_books` TINYINT(1) NOT NULL DEFAULT '0',
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `email` (`email`) USING BTREE,
                INDEX `invitation_code_id` (`invitation_code_id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("user_access_logs", """
            CREATE TABLE `user_access_logs` (
                `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
                `ip_address` TEXT NOT NULL COMMENT 'IP地址（兼容IPv4/IPv6）' COLLATE 'utf8mb4_0900_ai_ci',
                `visit_date` DATE NOT NULL COMMENT '访问日期（仅年月日）',
                `user_agent` TEXT NULL DEFAULT NULL COMMENT '用户浏览器标识' COLLATE 'utf8mb4_0900_ai_ci',
                PRIMARY KEY (`id`) USING BTREE,
                INDEX `idx_user_id_visit_date` (`user_id`, `visit_date`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        addMoreUserTables();
    }

    private static void addMoreUserTables() {
        TABLE_SQL_MAP.put("user_blacklist", """
            CREATE TABLE `user_blacklist` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `user_id` BIGINT(20) NOT NULL COMMENT '主动拉黑的用户',
                `blocked_id` BIGINT(20) NOT NULL COMMENT '被拉黑的用户',
                `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP() COMMENT '拉黑时间',
                `username` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `uk_user_block` (`user_id`, `blocked_id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("user_chapter_edit", """
            CREATE TABLE `user_chapter_edit` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `chapter_id` BIGINT(20) NOT NULL COMMENT '章节ID',
                `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
                `username` VARCHAR(255) NOT NULL COMMENT '用户名称' COLLATE 'utf8mb4_0900_ai_ci',
                `content` LONGBLOB NULL DEFAULT NULL COMMENT '用户修改后的章节内容（二进制大对象）',
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `uk_user_chapter` (`user_id`, `chapter_id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("user_feedback", """
            CREATE TABLE `user_feedback` (
                `id` INT(11) NOT NULL AUTO_INCREMENT,
                `user_id` INT(11) NOT NULL,
                `content` TEXT NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `is_delete` TINYINT(4) NOT NULL DEFAULT '0',
                `is_resolved` TINYINT(4) NOT NULL DEFAULT '0',
                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
                `chapter_id` BIGINT(20) NULL DEFAULT NULL,
                `novel_id` BIGINT(20) NULL DEFAULT NULL,
                PRIMARY KEY (`id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("user_glossary", """
            CREATE TABLE `user_glossary` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
                `novel_id` BIGINT(20) NOT NULL COMMENT '小说ID',
                `source_name` VARCHAR(255) NOT NULL COMMENT '源术语' COLLATE 'utf8mb4_unicode_ci',
                `target_name` VARCHAR(255) NOT NULL COMMENT '目标术语' COLLATE 'utf8mb4_unicode_ci',
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `uk_novel_source` (`novel_id`, `source_name`) USING BTREE,
                INDEX `idx_user` (`user_id`) USING BTREE,
                INDEX `idx_novel` (`novel_id`) USING BTREE,
                INDEX `idx_source` (`source_name`) USING BTREE
            )
            COLLATE='utf8mb4_unicode_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("user_novel_relation", """
            CREATE TABLE `user_novel_relation` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `user_id` BIGINT(20) NOT NULL,
                `novel_id` BIGINT(20) NOT NULL,
                `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP(),
                `platform` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `unique_user_novel` (`user_id`, `novel_id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("user_operation_log", """
            CREATE TABLE `user_operation_log` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
                `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
                `action` VARCHAR(255) NOT NULL COMMENT '操作名称' COLLATE 'utf8mb4_0900_ai_ci',
                `content` TEXT NULL DEFAULT NULL COMMENT '操作内容/JSON 或文本' COLLATE 'utf8mb4_0900_ai_ci',
                `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP() COMMENT '创建时间',
                PRIMARY KEY (`id`) USING BTREE,
                INDEX `idx_user_id` (`user_id`) USING BTREE,
                INDEX `idx_created_at` (`created_at`) USING BTREE
            )
            COMMENT='用户操作记录表'
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("user_tag_filter", """
            CREATE TABLE `user_tag_filter` (
                `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                `tag_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '标签ID',
                `user_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '用户ID',
                `tag_name` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                PRIMARY KEY (`id`) USING BTREE,
                INDEX `idx_user_id` (`user_id`) USING BTREE
            )
            COMMENT='用户标签过滤表'
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);
    }

    private static void addPostRelatedTables() {
        TABLE_SQL_MAP.put("post", """
            CREATE TABLE `post` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `title` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `author` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `content` TEXT NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `collections` BIGINT(20) NULL DEFAULT '0',
                `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP(),
                `post_type` INT(11) NOT NULL DEFAULT '0' COMMENT '0为推荐交流1为网站反馈',
                `comment_num` INT(11) NOT NULL DEFAULT '0',
                `user_id` BIGINT(20) NOT NULL,
                `recommended` TINYINT(4) NULL DEFAULT '1',
                `agree` INT(11) NOT NULL DEFAULT '0',
                `disagree` INT(11) NOT NULL DEFAULT '0',
                PRIMARY KEY (`id`) USING BTREE,
                INDEX `idx_post_type_recommended` (`post_type`, `recommended`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("post_agree", """
            CREATE TABLE `post_agree` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `post_id` BIGINT(20) NOT NULL COMMENT '帖子 id',
                `user_id` BIGINT(20) NOT NULL COMMENT '用户 id',
                `agree` TINYINT(1) NOT NULL COMMENT '1 赞同，0 不赞同',
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `uk_post_user` (`post_id`, `user_id`) USING BTREE,
                INDEX `idx_post` (`post_id`) USING BTREE,
                INDEX `idx_user` (`user_id`) USING BTREE
            )
            COMMENT='用户对帖子的赞同记录表'
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("post_comment", """
            CREATE TABLE `post_comment` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `post_id` BIGINT(20) NOT NULL,
                `username` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `avatar` VARCHAR(255) NULL DEFAULT 'https://via.placeholder.com/40' COLLATE 'utf8mb4_0900_ai_ci',
                `content` TEXT NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `likes` INT(11) NULL DEFAULT '0',
                `parent_id` BIGINT(20) NULL DEFAULT NULL,
                `reply_to` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `created_at` LONGBLOB NULL DEFAULT NULL,
                `user_id` BIGINT(20) NOT NULL,
                PRIMARY KEY (`id`) USING BTREE,
                INDEX `post_id` (`post_id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);
    }

    private static void addMiscTables() {
        TABLE_SQL_MAP.put("platform", """
            CREATE TABLE `platform` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `platform_name` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `platform_type` VARCHAR(64) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                PRIMARY KEY (`id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("platform_api_key", """
            CREATE TABLE `platform_api_key` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `platform_id` BIGINT(20) NOT NULL,
                `api_key` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `is_deleted` TINYINT(1) NULL DEFAULT '0',
                PRIMARY KEY (`id`) USING BTREE,
                INDEX `platform_id` (`platform_id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("reading_record", """
            CREATE TABLE `reading_record` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `user_id` BIGINT(20) NOT NULL,
                `novel_id` BIGINT(20) NOT NULL,
                `last_chapter` BIGINT(20) NOT NULL,
                `last_chapter_id` BIGINT(20) NULL DEFAULT NULL,
                `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `uk_user_novel` (`user_id`, `novel_id`) USING BTREE,
                INDEX `idx_user_update` (`user_id`, `update_time` DESC) USING BTREE,
                INDEX `idx_novel` (`novel_id`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("request_log", """
            CREATE TABLE `request_log` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `credential_id` BIGINT(20) NOT NULL,
                `request_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP(),
                `count` INT(11) NULL DEFAULT '0',
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `idx_request_log_unique_credential_time` (`credential_id`, `request_time`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("tag", """
            CREATE TABLE `tag` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `name` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP(),
                `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
                `true_name` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `platform` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                PRIMARY KEY (`id`) USING BTREE,
                UNIQUE INDEX `uk_platform_true_name` (`platform`, `true_name`) USING BTREE,
                INDEX `unique_true_name` (`true_name`) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);

        TABLE_SQL_MAP.put("terminology", """
            CREATE TABLE `terminology` (
                `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                `novel_id` INT(11) NOT NULL,
                `novel_true_id` VARCHAR(128) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `chapter_true_id` VARCHAR(64) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `source_name` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `target_name` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
                `chapter_num` INT(11) NULL DEFAULT NULL,
                PRIMARY KEY (`id`) USING BTREE,
                INDEX `idx_novel_chapter` (`novel_id`) USING BTREE,
                INDEX `idx_novel_chapter_true_id` (`novel_true_id`, `chapter_true_id`) USING BTREE,
                INDEX `idx_novel_chapter_num` (`novel_id`, `chapter_num`) USING BTREE,
                INDEX `idx_source_name` (`source_name`(100)) USING BTREE
            )
            COLLATE='utf8mb4_0900_ai_ci'
            ENGINE=InnoDB
            ROW_FORMAT=DYNAMIC
            AUTO_INCREMENT=1
            """);
    }
}
