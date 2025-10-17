package com.wtl.novel.util;


import java.io.BufferedReader;
import java.nio.file.*;
import java.sql.*;

public class UpdateNovelFromFile {

    /* ========== 配置 ========== */
    private static final Path INPUT_FILE = Paths.get("novelpia_result.csv"); // 输出文件

    /* ========== SQL ========== */
    private static final String UPDATE_SQL =
            "UPDATE novel SET novel_like = ?, novel_read = ?, author_id = ?, author_name = ? WHERE id = ?";

    /* ========== 批量大小 ========== */
    private static final int BATCH_SIZE = 1000;

    public static void main(String[] args) {
        // 添加关闭钩子,确保程序退出时关闭连接池
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnectionUtil::shutdown));
        
        try (Connection conn = DatabaseConnectionUtil.getPrimaryConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL);
             BufferedReader br = Files.newBufferedReader(INPUT_FILE)) {

            conn.setAutoCommit(false);          // 手动事务
            String line;
            int count = 0;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] arr = line.split("&", -1);   // 按&切分
                if (arr.length < 5) continue;         // 格式不对直接跳过

                String id        = arr[0].trim();
                String like      = arr[1].replace(",", "").trim();
                String read      = arr[2].replace(",", "").trim(); // 去掉千位分隔符
                String authorId  = arr[3].trim();
                String authorName= arr[4].trim();

                ps.setLong  (1, Long.parseLong(like));
                ps.setLong  (2, Long.parseLong(read));
                ps.setString(3, authorId);
                ps.setString(4, authorName);
                ps.setLong  (5, Long.parseLong(id));
                ps.addBatch();

                if (++count % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                }
            }
            // 提交剩余
            ps.executeBatch();
            conn.commit();

            System.out.println("全部更新完成，共 " + count + " 条");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}