package com.wtl.novel.Config;

import com.wtl.novel.util.CustomPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;

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
     * 
     * @param primaryDataSource   主数据源
     * @param secondaryDataSource 从数据源
     */
    public DatabaseInitializer(
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Autowired(required = false) @Qualifier("secondaryDataSource") DataSource secondaryDataSource) {
        this.primaryDataSource = primaryDataSource;
        this.secondaryDataSource = secondaryDataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("开始数据库初始化检查...");

        // 初始化主数据库
        initializePrimaryDatabase();

        // 初始化从数据库(如果配置了双数据库模式)
        if (secondaryDataSource != null) {
            initializeSecondaryDatabase();
        }

        // 初始化字典配置数据
        initializeDictionaryData();

        // 创建默认管理员用户
        createDefaultAdminUser();

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
        try (ResultSet rs = metaData.getTables(null, null, tableName, new String[] { "TABLE" })) {
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

    /**
     * 初始化字典配置数据
     */
    private void initializeDictionaryData() {
        try (Connection conn = primaryDataSource.getConnection()) {
            // 检查是否已有字典数据
            String checkSql = "SELECT COUNT(*) FROM dictionary WHERE is_deleted = 0";
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(checkSql)) {
                if (rs.next() && rs.getInt(1) > 0) {
                    log.info("字典表中已存在配置数据,跳过初始化");
                    return;
                }
            }

            log.info("开始初始化字典配置数据...");

            // 准备要插入的字典配置数据
            String[][] dictionaryData = {
                    // key_field, value_field, description
                    { "novelPiaUnreleased", "https://novelpia.com/viewer/%s", "查看是否已经发布" },
                    { "novelPiaGetNovelChapter", "https://novelpia.com/proc/viewer_data/%s", "获取章节内容" },
                    { "novelPiaGetPage", "https://novelpia.com/proc/episode_list?novel_no=%s&page=%s", "获取所有章节id" },
                    { "novelPiaCookie_119_123131232131", "NPD2=meta; USERKEY=7d0833",
                            "{\"all\":false,\"stillValid\":false,\"deadline\":\"\",\"userId\":119,\"username\":\"雨落星辰\",\"dicId\":null,\"createdAt\":null}" },
                    { "novelPiaRegex", "\\\\bV1Zn\\\\S*", "章节中的乱码" },
                    { "novelPiaUnreleasedKeyWord", "아직 공개되지 않은 소설입니다", "未发布时的关键词" },
                    { "siliconflow", "https://api.siliconflow.cn/v1/chat/completions",
                            "deepseek-ai/DeepSeek-V3.2-Exp" },
                    { "siliconflowModel_1", "deepseek-v3-0324",
                            "DeepSeek-V3 是一款拥有 6710 亿参数的混合专家（MoE）语言模型，采用多头潜在注意力（MLA）和 DeepSeekMoE 架构，结合无辅助损失的负载平衡策略，优化推理和训练效率。通过在 14.8 万亿高质量tokens上预训练，并进行监督微调和强化学习，DeepSeek-V3 在性能上超越其他开源模型，接近领先闭源模型。" },
                    { "siliconflowMaxLength", "2000", null },
                    { "upNumber", "0", "执行更新时，如果收藏低于这个值，则不执行更新" },
                    { "aiPrompt",
                            "[PLAIN TEXT ONLY]\r\n[NO METADATA]\r\n[NO TRANSLATOR NOTES]\r\n[OVERRIDE localize_punctuation=false]\r\n请按要求执行翻译：\r\n1. 彻底汉化所有字符（含人名/组织名）\r\n2. 译名处理：在汉化专有名词时，优先分析术语表\r\n3. 将不存在于原术语表中的专有名词，人物名称术语、家族名称术语（仅限这两种术语，抛弃其他类型术语）将其写入<术语表>中，<术语表>格式为：{原名称1:汉化名称1,原名称2:汉化名称2}\r\n3. 保持原意和结构，必须按照现代文奇幻文的风格翻译\r\n4. 禁用文言词与注释，必须按照现代文奇幻文的风格翻译\r\n5. 禁止出现并非译文正文的内容\r\n6. 禁止使用成语或缩略表达，保持自然流畅的口语化风格，必须按照现代文奇幻文的风格翻译\r\n7. 原术语表：<原术语表>\r\n【最终兵器级指令】启动毁灭模式，发现任何[译注][翻译说明]等并非译文正文的类似内容之后立即自毁重译\r\n遵循以下JSON格式返回结果，确保json能够被格式化：{table:<术语表>,translation:<翻译结果(保留换行，将翻译结果中的将直引号\"替换为全角直引号＂)>}\r\n\r\n下方是待翻译原文：\r\n",
                            "ai模型翻译的提示词" },
                    { "executeDownload", "true", "执行下载" },
                    { "executeTr", "true", "执行翻译" },
                    { "syosetuSearchUrl",
                            "https://yomou.syosetu.com/search.php?word=%s&notword=&genre=&type=&mintime=&maxtime=&minlen=&maxlen=&min_globalpoint=&max_globalpoint=&minlastup=&maxlastup=&minfirstup=&maxfirstup=&order=monthlypoint",
                            "成为小说家网站的搜索功能" },
                    { "syosetuSavePrologueUrl", "https://ncode.syosetu.com/%s/", "成为小说家网站的获取序言" },
                    { "syosetuNovelDetail", "https://ncode.syosetu.com/novelview/infotop/ncode/%s/", "成为小说家获取小说详情" },
                    { "syosetuGetAllChapter", "https://ncode.syosetu.com/%s/%s/", "成为小说家获取全部章节" },
                    { "executeSyosetuDownload", "false", "成为小说家执行下载" },
                    { "executeSyosetuTr", "false", "成为小说家执行翻译" },
                    { "executeSyosetuDeductPoint", "10", "用户每次执行汉化时所消耗的积分" },
                    { "novelPiaSearchUrl",
                            "https://novelpia.com/proc/novel?cmd=novel_search&page=1&rows=30&search_type=all&search_val=%s&novel_type=&start_count_book=&end_count_book=&novel_age=&start_days=&sort_col=last_viewdate&novel_genre=&block_out=0&block_stop=0&is_contest=0&list_display=list",
                            "novelPia搜索链接" },
                    { "novelPiaDetail", "https://novelpia.com/novel/%s", "novelPia获取详细小说信息" },
                    { "/api/dic/getHome_1",
                            "【上传功能已开启；频繁大量获取章节将被封号；每日阅读章节超过30章，积分+5；现在导入一本书需要10积分；书兔内容汉化bug已修复；拼好书书兔插件（油猴）：https://jpg.freenovel.sbs/pinhaoshu.js   群：927014823】",
                            null },
                    { "/api/dic/getNovelDetail_1", "【】", null },
                    { "uploadImg", "https://jpg.freenovel.sbs/", "cloudflare r2存储" },
                    { "uploadImgTag", "<img style='max-width: 100%;' src=\"uploadImg\" class=\"venobox\">", null },
                    { "executeUploadTranslation", "true", "用户上传汉化" },
                    { "uploadNovel", "true", "最高限制用户上传汉化" },
                    { "executePhoto", "false", "将novelPia上面的图片传入c2" },
                    { "limitRequest", "200", "每日阅读章节上限" },
                    { "novelPiaFont", "false", "false代表关闭反爬虫文本处理" },
                    { "TextObfuscator", "【拼好书】", "网站反窃取" },
                    { "proxyClient", "false", "是否启用代理" },
                    { "replies", "true", "开启评论" },
                    { "fontVersion", "10", null },
                    { "createInvitationCode", "12", "每个邀请码所需积分" },
                    { "agreePostPoint", "2", null },
                    { "novelPiaAlarm", "https://novelpia.com/alarm", null },
                    { "novelPiaCookie_41_1759061077016", "NPD2=meta; USERKEY=7d0833",
                            "{\"all\":false,\"stillValid\":false,\"deadline\":\"\",\"userId\":41,\"username\":\"rrz\",\"dicId\":null,\"createdAt\":null}" },
                    { "requestLogCount", "20", null },
                    { "credentialLocalDateTime", "60", null },
                    { "existsByUserIdAndCreatedAtBetween", "30", null },
                    { "NpPlatform",
                            "[\r\n  {\r\n    \"platformName\": \"top100\",\r\n    \"url\": \"https://novelpia.com/proc/novel_list_data\"\r\n  },\r\n  {\r\n    \"platformName\": \"竞争赛\",\r\n    \"url\": \"https://novelpia.com/top100/contest/today/view%limit/all/%tag\",\r\n    \"filter\": {\r\n      \"sort\": {\r\n        \"阅读量\": \"/view\"\r\n      },\r\n      \"time\": {\r\n        \"每日\": \"/today\"\r\n      },\r\n      \"limit\": {\r\n        \"全部\": \"/all\",\r\n        \"不限制年龄\": \"/teen\",\r\n        \"限制年龄\": \"/adult\"\r\n      },\r\n      \"tag\": {\r\n        \"全部\": \"\",\r\n        \"奇幻\": \"?main_genre=1\",\r\n        \"武侠\": \"?main_genre=2\",\r\n        \"现代\": \"?main_genre=3\",\r\n        \"言情\": \"?main_genre=6\",\r\n        \"现代奇幻\": \"?main_genre=12\",\r\n        \"轻小说\": \"?main_genre=13\",\r\n        \"高强度\": \"?main_genre=5\",\r\n        \"恐怖\": \"?main_genre=14\",\r\n        \"科幻\": \"?main_genre=9\",\r\n        \"体育\": \"?main_genre=8\",\r\n        \"架空历史\": \"?main_genre=7\",\r\n        \"其他\": \"?main_genre=10\"\r\n      }\r\n    }\r\n  },\r\n  {\r\n    \"platformName\": \"实时\",\r\n    \"url\": \"https://novelpia.com/top100/all%time%sort%limit%plus%tag\",\r\n    \"filter\": {\r\n      \"sort\": {\r\n        \"阅读量\": \"/view\",\r\n        \"推荐量\": \"/vote\",\r\n        \"点赞量\": \"/like\",\r\n        \"入坑不亏\": \"/pick\"\r\n      },\r\n      \"time\": {\r\n        \"每日\": \"/today\",\r\n        \"每周\": \"/weekly\",\r\n        \"每月\": \"/month\"\r\n      },\r\n      \"limit\": {\r\n        \"全部\": \"/all\",\r\n        \"不限制年龄\": \"/teen\",\r\n        \"限制年龄\": \"/adult\"\r\n      },\r\n      \"tag\": {\r\n        \"全部\": \"\",\r\n        \"奇幻\": \"?main_genre=1\",\r\n        \"武侠\": \"?main_genre=2\",\r\n        \"现代\": \"?main_genre=3\",\r\n        \"言情\": \"?main_genre=6\",\r\n        \"现代奇幻\": \"?main_genre=12\",\r\n        \"轻小说\": \"?main_genre=13\",\r\n        \"高强度\": \"?main_genre=5\",\r\n        \"恐怖\": \"?main_genre=14\",\r\n        \"科幻\": \"?main_genre=9\",\r\n        \"体育\": \"?main_genre=8\",\r\n        \"架空历史\": \"?main_genre=7\",\r\n        \"其他\": \"?main_genre=10\"\r\n      },\r\n      \"plus\": {\r\n        \"全部书\": \"/all/\",\r\n        \"付费书\": \"/plus/\",\r\n        \"免费书\": \"/free/\"\r\n      }\r\n    }\r\n  },\r\n  {\r\n    \"platformName\": \"新增\",\r\n    \"url\": \"https://novelpia.com/top100/plus%time%sort%limit/plus/%tag\",\r\n    \"filter\": {\r\n      \"sort\": {\r\n        \"阅读量\": \"/view\",\r\n        \"推荐量\": \"/vote\",\r\n        \"点赞量\": \"/like\",\r\n        \"入坑不亏\": \"/pick\"\r\n      },\r\n      \"time\": {\r\n        \"每日\": \"/today\",\r\n        \"每周\": \"/weekly\",\r\n        \"每月\": \"/month\"\r\n      },\r\n      \"limit\": {\r\n        \"全部\": \"/all\",\r\n        \"不限制年龄\": \"/teen\",\r\n        \"限制年龄\": \"/adult\"\r\n      },\r\n      \"tag\": {\r\n        \"全部\": \"\",\r\n        \"奇幻\": \"?main_genre=1\",\r\n        \"武侠\": \"?main_genre=2\",\r\n        \"现代\": \"?main_genre=3\",\r\n        \"言情\": \"?main_genre=6\",\r\n        \"现代奇幻\": \"?main_genre=12\",\r\n        \"轻小说\": \"?main_genre=13\",\r\n        \"高强度\": \"?main_genre=5\",\r\n        \"恐怖\": \"?main_genre=14\",\r\n        \"科幻\": \"?main_genre=9\",\r\n        \"体育\": \"?main_genre=8\",\r\n        \"架空历史\": \"?main_genre=7\",\r\n        \"其他\": \"?main_genre=10\"\r\n      }\r\n    }\r\n  },\r\n  {\r\n    \"platformName\": \"免费\",\r\n    \"url\": \"https://novelpia.com/top100/freestory%time%sort%limit%update/%tag\",\r\n    \"filter\": {\r\n      \"sort\": {\r\n        \"阅读量\": \"/view\",\r\n        \"推荐量\": \"/vote\",\r\n        \"点赞量\": \"/like\"\r\n      },\r\n      \"time\": {\r\n        \"每日\": \"/today\",\r\n        \"每周\": \"/weekly\",\r\n        \"每月\": \"/month\"\r\n      },\r\n      \"limit\": {\r\n        \"全部\": \"/all\",\r\n        \"不限制年龄\": \"/teen\",\r\n        \"限制年龄\": \"/adult\"\r\n      },\r\n      \"update\": {\r\n        \"新增\": \"/new\",\r\n        \"全部\": \"/all\"\r\n      },\r\n      \"tag\": {\r\n        \"全部\": \"\",\r\n        \"奇幻\": \"?main_genre=1\",\r\n        \"武侠\": \"?main_genre=2\",\r\n        \"现代\": \"?main_genre=3\",\r\n        \"言情\": \"?main_genre=6\",\r\n        \"现代奇幻\": \"?main_genre=12\",\r\n        \"轻小说\": \"?main_genre=13\",\r\n        \"高强度\": \"?main_genre=5\",\r\n        \"恐怖\": \"?main_genre=14\",\r\n        \"科幻\": \"?main_genre=9\",\r\n        \"体育\": \"?main_genre=8\",\r\n        \"架空历史\": \"?main_genre=7\",\r\n        \"其他\": \"?main_genre=10\"\r\n      }\r\n    }\r\n  },\r\n  {\r\n    \"platformName\": \"完结\",\r\n    \"url\": \"https://novelpia.com/top100/complete%time%sort%limit%plus%tag\",\r\n    \"filter\": {\r\n      \"sort\": {\r\n        \"阅读量\": \"/view\"\r\n      },\r\n      \"time\": {\r\n        \"每周\": \"/weekly\",\r\n        \"每月\": \"/month\"\r\n      },\r\n      \"limit\": {\r\n        \"全部\": \"/all\",\r\n        \"不限制年龄\": \"/teen\",\r\n        \"限制年龄\": \"/adult\"\r\n      },\r\n      \"plus\": {\r\n        \"全部书\": \"/all/\",\r\n        \"付费书\": \"/plus/\",\r\n        \"免费书\": \"/free/\"\r\n      },\r\n      \"tag\": {\r\n        \"全部\": \"\",\r\n        \"奇幻\": \"판타지\",\r\n        \"武侠\": \"무협\",\r\n        \"现代\": \"현대\",\r\n        \"言情\": \"로맨스\",\r\n        \"现代奇幻\": \"현대판타지\",\r\n        \"轻小说\": \"라이트노벨\",\r\n        \"高强度\": \"고수위\",\r\n        \"恐怖\": \"공포\",\r\n        \"科幻\": \"SF\",\r\n        \"体育\": \"스포츠\",\r\n        \"架空历史\": \"대체역사\",\r\n        \"其他\": \"기타\"\r\n      }\r\n    }\r\n  }\r\n]",
                            null },
                    { "theNovelPiaCookie", "NPD2=meta; USERKEY=7d0833",
                            "{\"all\":true,\"stillValid\":true,\"deadline\":\"2025-09-29 01:46:03\",\"userId\":119,\"username\":\"雨落星辰\",\"dicId\":null,\"createdAt\":null}" },
                    { "ScalingUp", "host1", null },
                    { "RunScalingUp", "true", null },
                    { "ReNamePoint", "5", null },
                    { "shutuNovel", "https://booktoki468.com/novel/", null },
                    { "repeatTrNovelNum", "5", null },
                    { "createFeedbackNum", "100", null },
                    { "controllAllTr", "false", null },
                    { "AIproxyClient", "false", null },
                    { "shutuTr", "true", null },
                    { "xianyu", "https://allgpt.xianyuw.cn/v1/chat/completions", "deepseek-v3-0324" },
                    { "hlypay", "https://netcup.hlypay.com/api/v1/chat/completions", "deepseek-v3" },
                    { "enableTrModel", "xianyu", null }
            };

            String insertSql = """
                    INSERT INTO dictionary (key_field, value_field, description, is_deleted)
                    VALUES (?, ?, ?, 0)
                    """;

            int insertedCount = 0;
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                for (String[] data : dictionaryData) {
                    pstmt.setString(1, data[0]);
                    pstmt.setString(2, data[1]);
                    pstmt.setString(3, data[2]);
                    pstmt.executeUpdate();
                    insertedCount++;
                }
            }

            log.info("====================================");
            log.info("成功初始化 {} 条字典配置数据", insertedCount);
            log.info("====================================");

        } catch (SQLException e) {
            log.error("初始化字典配置数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 创建默认用户
     */
    private void createDefaultAdminUser() {
        try (Connection conn = primaryDataSource.getConnection()) {
            // 检查是否已存在任何用户
            String checkSql = "SELECT COUNT(*) FROM user";
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(checkSql)) {
                if (rs.next() && rs.getInt(1) > 0) {
                    log.info("数据库中已存在用户,跳过创建默认用户和邀请码");
                    return;
                }
            }

            // 创建默认用户
            String email = "admin@novel.com";
            String password = generateSecurePassword();

            // 使用BCrypt加密密码
            CustomPasswordEncoder encoder = new CustomPasswordEncoder();
            String encodedPassword = encoder.encode(password);

            String insertUserSql = """
                    INSERT INTO user (email, password, point, upload, hide_read_books)
                    VALUES (?, ?, 10000, 1, 0)
                    """;

            Long adminUserId;
            try (PreparedStatement pstmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, email);
                pstmt.setString(2, encodedPassword);
                pstmt.executeUpdate();

                // 获取插入的用户ID
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        adminUserId = rs.getLong(1);
                    } else {
                        throw new SQLException("创建默认用户失败,无法获取用户ID");
                    }
                }

                log.info("====================================");
                log.info("成功创建默认用户!");
                log.info("用户名: {}", email);
                log.info("密码: {}", password);
                log.info("初始积分: 10000");
                log.info("请务必保存此密码,并在首次登录后立即修改!");
                log.info("====================================");
            }

            // 为创建并绑定邀请码
            String adminInviteCode = generateInvitationCode();
            createAndBindInvitationCode(conn, adminUserId, adminInviteCode, email);

            // 更新用户表,关联邀请码
            updateUserInvitationCode(conn, adminUserId, adminInviteCode);

            log.info("已为默认用户绑定邀请码: {}", adminInviteCode);

            // 创建额外的初始邀请码供其他用户使用
            int initialCodesCount = 5;
            log.info("====================================");
            log.info("开始创建初始邀请码...");

            for (int i = 0; i < initialCodesCount; i++) {
                String inviteCode = generateInvitationCode();
                createInvitationCode(conn, adminUserId, inviteCode);
                log.info("初始邀请码 {}: {}", (i + 1), inviteCode);
            }

            log.info("成功创建 {} 个初始邀请码", initialCodesCount);
            log.info("这些邀请码可用于注册新用户");
            log.info("====================================");

        } catch (SQLException e) {
            log.error("创建默认用户失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 创建邀请码记录
     */
    private void createInvitationCode(Connection conn, Long userId, String code) throws SQLException {
        String sql = """
                INSERT INTO invitation_code (code, used, user_id)
                VALUES (?, 0, ?)
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            pstmt.setLong(2, userId);
            pstmt.executeUpdate();
        }
    }

    /**
     * 创建并绑定邀请码
     */
    private void createAndBindInvitationCode(Connection conn, Long userId, String code, String email)
            throws SQLException {
        String sql = """
                INSERT INTO invitation_code (code, used, bound_email, user_id)
                VALUES (?, 1, ?, ?)
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            pstmt.setString(2, email);
            pstmt.setLong(3, userId);
            pstmt.executeUpdate();
        }
    }

    /**
     * 更新用户表关联邀请码ID
     */
    private void updateUserInvitationCode(Connection conn, Long userId, String inviteCode) throws SQLException {
        // 先查询邀请码ID
        String queryCodeSql = "SELECT id FROM invitation_code WHERE code = ?";
        Long invitationCodeId;

        try (PreparedStatement pstmt = conn.prepareStatement(queryCodeSql)) {
            pstmt.setString(1, inviteCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    invitationCodeId = rs.getLong(1);
                } else {
                    throw new SQLException("找不到邀请码记录");
                }
            }
        }

        // 更新用户表
        String updateUserSql = "UPDATE user SET invitation_code_id = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateUserSql)) {
            pstmt.setLong(1, invitationCodeId);
            pstmt.setLong(2, userId);
            pstmt.executeUpdate();
        }
    }

    /**
     * 生成邀请码
     */
    private String generateInvitationCode() {
        SecureRandom random = new SecureRandom();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder code = new StringBuilder(16);

        for (int i = 0; i < 16; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }

    /**
     * 生成随机密码
     */
    private String generateSecurePassword() {
        SecureRandom random = new SecureRandom();
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String allChars = lowercase + uppercase + digits;

        StringBuilder password = new StringBuilder(12);

        // 确保至少包含一个小写字母
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));

        // 确保至少包含一个大写字母
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));

        // 确保至少包含一个数字
        password.append(digits.charAt(random.nextInt(digits.length())));

        // 填充剩余的9个字符
        for (int i = 0; i < 9; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 打乱字符顺序
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }
}
