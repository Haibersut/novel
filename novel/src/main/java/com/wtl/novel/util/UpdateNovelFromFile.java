package com.wtl.novel.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.nio.file.*;
import java.sql.*;

@Component
public class UpdateNovelFromFile {

    private static final Logger log = LoggerFactory.getLogger(UpdateNovelFromFile.class);

    /* ========== 配置 ========== */
    private static final Path INPUT_FILE = Paths.get("novelpia_result.csv"); // 输出文件

    /* ========== SQL ========== */
    private static final String UPDATE_SQL =
            "UPDATE novel SET novel_like = ?, novel_read = ?, author_id = ?, author_name = ? WHERE id = ?";

    /* ========== 批量大小 ========== */
    private static final int BATCH_SIZE = 1000;

    private final DataSource dataSource;

    public UpdateNovelFromFile(@Qualifier("primaryDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 执行更新任务
     */
    public void execute() {
        log.info("开始执行从文件更新小说数据任务");
        
        // 检查文件是否存在
        if (Files.notExists(INPUT_FILE)) {
            log.error("输入文件不存在: {}", INPUT_FILE.toAbsolutePath());
            return;
        }
        
        Connection conn = null;
        PreparedStatement ps = null;
        BufferedReader br = null;
        
        try {
            conn = dataSource.getConnection();
            if (conn == null) {
                log.error("无法获取数据库连接");
                return;
            }
            
            ps = conn.prepareStatement(UPDATE_SQL);
            br = Files.newBufferedReader(INPUT_FILE);
            
            conn.setAutoCommit(false);          // 手动事务
            String line;
            int count = 0;
            int errorCount = 0;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                
                String[] arr = line.split("&", -1);   // 按&切分
                if (arr.length < 5) {
                    log.warn("数据格式不正确，跳过: {}", line);
                    errorCount++;
                    continue;
                }

                try {
                    String id = arr[0].trim();
                    String like = arr[1].replace(",", "").trim();
                    String read = arr[2].replace(",", "").trim();
                    String authorId = arr[3].trim();
                    String authorName = arr[4].trim();

                    // 验证必要字段不为空
                    if (id.isEmpty()) {
                        log.warn("ID为空，跳过此行");
                        errorCount++;
                        continue;
                    }

                    ps.setLong(1, like.isEmpty() ? 0L : Long.parseLong(like));
                    ps.setLong(2, read.isEmpty() ? 0L : Long.parseLong(read));
                    ps.setString(3, authorId);
                    ps.setString(4, authorName);
                    ps.setLong(5, Long.parseLong(id));
                    ps.addBatch();

                    if (++count % BATCH_SIZE == 0) {
                        ps.executeBatch();
                        conn.commit();
                        log.debug("已提交 {} 条记录", count);
                    }
                } catch (NumberFormatException e) {
                    log.error("数字格式错误，跳过: {}", line, e);
                    errorCount++;
                }
            }
            
            // 提交剩余
            if (count % BATCH_SIZE != 0) {
                ps.executeBatch();
                conn.commit();
            }

            log.info("全部更新完成，共 {} 条成功，{} 条失败", count, errorCount);
        } catch (Exception e) {
            log.error("更新数据失败", e);
            if (conn != null) {
                try {
                    conn.rollback();
                    log.info("已回滚事务");
                } catch (SQLException rollbackEx) {
                    log.error("回滚失败", rollbackEx);
                }
            }
        } finally {
            // 关闭资源
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    log.error("关闭BufferedReader失败", e);
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    log.error("关闭PreparedStatement失败", e);
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // 恢复自动提交
                    conn.close();
                } catch (SQLException e) {
                    log.error("关闭数据库连接失败", e);
                }
            }
        }
    }
}