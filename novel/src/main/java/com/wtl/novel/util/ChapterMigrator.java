package com.wtl.novel.util;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import javax.imageio.ImageIO;
import javax.sql.DataSource;

import com.wtl.novel.entity.ChapterImageLink;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ChapterMigrator {
    // 数据库配置
    private static final String JDBC_URL = "jdbc:mysql://*.*.*.*:3306/novel";
    private static final String USER = "";
    private static final String PASSWORD = "";

    // 连接池配置
    private static final int MAX_POOL_SIZE = 20;
    private static final int MIN_IDLE = 10;

    // 迁移参数
    private static final int BATCH_SIZE = 200;
    private static final int PAGE_SIZE = 1000;

    private static DataSource dataSource;
    private static long lastId = 0; // 修复: 声明为类变量

    static {
        // 初始化连接池
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(JDBC_URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(MAX_POOL_SIZE);
        config.setMinimumIdle(MIN_IDLE);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "500");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "4096");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        config.setConnectionTimeout(60000);
        config.setIdleTimeout(600000);
        config.setLeakDetectionThreshold(30000);

        dataSource = new HikariDataSource(config);
    }

    

    public static void extractChaptersToFiles(long novelId, String outputDir,String name) throws Exception {
        Path outputPath = Paths.get(outputDir);
        // 修改查询语句，添加own_photo和true_id字段
        String query = "SELECT id, title, chapter_number, content, own_photo, true_id FROM chapter WHERE novel_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, novelId);
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;

                while (rs.next()) {
                    byte[] compressedContent = rs.getBytes("content");
                    boolean ownPhoto = rs.getBoolean("own_photo");
                    String trueId = rs.getString("true_id");

                    // 解压缩内容
                    String content = CompressionUtils.decompress(compressedContent);
                    String regex = "[\u200B\u200C\u200D\uFEFF]";
                    content = content.replaceAll(regex, "");
                    content = StringEncoder.cleanText(content);
                    content = "第" + count + "章\n" + content + "\n";
                    // 如果own_photo为1，处理图片链接
                    if (ownPhoto && trueId != null && !trueId.isEmpty()) {
                        content = insertImageLinks(conn, content, trueId,name);
                    }

                    // 写入文件
                    Files.write(outputPath,
                            (content + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND);
                    count++;

                    System.out.printf("已导出: %s (%d 字节)%n",
                            outputPath, content.length());
                }

                System.out.printf("共导出 %d 个章节到目录: %s%n", count, outputPath.toAbsolutePath());
            }
        }
    }

    // 插入图片链接到内容中
    private static String insertImageLinks(Connection conn, String content, String trueId, String name) throws SQLException {
        // 查询图片链接
        String imgQuery = "SELECT content_link, location FROM chapter_image_links " +
                "WHERE chapter_true_id = ? ORDER BY location";
        List<ImageInsertion> insertions = new ArrayList<>();

        try (PreparedStatement imgStmt = conn.prepareStatement(imgQuery)) {
            imgStmt.setString(1, trueId);
            try (ResultSet imgRs = imgStmt.executeQuery()) {
                while (imgRs.next()) {
                    String link = imgRs.getString("content_link");
                    String location = imgRs.getString("location");
                    insertions.add(new ImageInsertion(link, location));
                }
            }
        }

        if (insertions.isEmpty()) {
            return content;
        }

        // 用于跟踪已处理的文件名
        Set<String> processedFileNames = new HashSet<>();

        // 使用迭代器以便在遍历时安全删除
        Iterator<ImageInsertion> iterator = insertions.iterator();

        while (iterator.hasNext()) {
            ImageInsertion insertion = iterator.next();
            try {
                // 从原img标签中提取src
                String src = extractSrc(insertion.getLink());
                if (src == null) continue;

                // 修复协议缺失问题
                if (src.startsWith("//")) {
                    src = "https:" + src;
                } else if (!src.startsWith("http")) {
                    src = "https://" + src;
                }

                // 提取文件名
                String fileName = getFileNameFromUrl(src);

                // 检查文件名是否已存在
                if (processedFileNames.contains(fileName)) {
                    // 如果文件名已存在，移除当前对象
                    iterator.remove();
                    continue;
                }

                // 标记此文件名已处理
                processedFileNames.add(fileName);

                // 下载图片到本地
                URL imageUrl = new URL(src);
                String localPath = "C:\\Users\\30402\\Desktop\\新建文件夹 (6)\\output\\image\\" +
                        name + File.separator + fileName;

                // 确保目录存在
                new File(localPath).getParentFile().mkdirs();
                String s = downloadImage(imageUrl, localPath);

                // 获取图片尺寸
                BufferedImage image = ImageIO.read(new File(s));
                int width = image.getWidth() * 100;
                int height = image.getHeight() * 100;

                // 生成新img标签
                String[] split = fileName.split("\\.");
                if (Objects.equals(split[1], "file")) {
                    split[1] = "png";
                }
                String newSrc = "https://novelkit.top/images/" + name + "/" + split[0] + "/" + split[1];
                String newImgTag = String.format(
                        "<img src=\"%s\" style=\"width:%dpx; height:%dpx;\">",
                        newSrc, width, height
                );

                // 替换原link内容
                insertion.setLink(newImgTag);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(insertion.getLink());
                // 出错时保留原链接
                insertion.setLink(insertion.getLink());
            }
        }

        return insertContentLinks(content, insertions);
    }

    // 辅助方法：从img标签中提取src
    private static String extractSrc(String imgTag) {
        int srcIndex = imgTag.indexOf("src=\"");
        if (srcIndex == -1) return null;
        int start = srcIndex + 5;
        int end = imgTag.indexOf("\"", start);
        return end > start ? imgTag.substring(start, end) : null;
    }

    // 辅助方法：从URL获取文件名
    private static String getFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    // 辅助方法：下载图片
    private static String downloadImage(URL url, String filePath) throws Exception {
        if (filePath.endsWith(".file")) {
            filePath = filePath.substring(0, filePath.lastIndexOf('.')) + ".png";
        }
        File file = new File(filePath);

        // 检查文件是否已存在
        if (file.exists()) {
            System.out.println("文件已存在，跳过下载: " + filePath);
            return filePath; // 直接返回，不执行后续下载
        }

        // 确保目录存在
        File dir = file.getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("无法创建目录: " + dir.getAbsolutePath());
            }
        }

        // 下载文件
        try (InputStream in = url.openStream()) {
            Files.copy(in, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("文件下载完成: " + filePath);
        }
        return filePath;
    }

    private static class ImageInsertion {
         String link;
         String position;

        ImageInsertion(String link, String position) {
            this.link = link;
            this.position = position;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public String getLink() {
            return link;
        }

        public String getPosition() {
            return position;
        }
    }
    public static String insertContentLinks(String content, List<ImageInsertion> chapterImageLinks) {
        // 将字符串按行分割
        String[] lines = content.split("\n");
        List<String> resultLines = new ArrayList<>();

        // 按照位置插入 contentLink
        for (int i = 0; i < lines.length; i++) {
            resultLines.add(lines[i]);

            // 检查当前行是否需要插入 contentLink
            for (ImageInsertion imageLink : chapterImageLinks) {
                String location = imageLink.getPosition();
                if (location != null && location.contains("_")) {
                    String[] parts = location.split("_");
                    int numerator = Integer.parseInt(parts[0]);
                    int denominator = Integer.parseInt(parts[1]);
                    int position = (int) Math.round((double) numerator / denominator * lines.length);

                    // 如果当前行是插入位置，插入 contentLink
                    if (i == position) {
                        resultLines.add(imageLink.getLink());
                    }
                }
            }
        }

        // 返回插入后的字符串
        return String.join("\n", resultLines);
    }


    private static String sanitizeFilename(String name) {
        if (name == null) return "untitled";
        // 移除非法字符，保留中文、英文、数字和基本符号
        return name.replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", " ")
                .trim();
    }

//    public static void main(String[] args) {
//        long startTime = System.currentTimeMillis();
//
//        try {
//            migrateChapterData();
//            System.out.println("数据迁移完成!");
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            System.out.println("总耗时: " + (System.currentTimeMillis() - startTime) / 1000 + " 秒");
//        }
//    }

    private static void migrateChapterData() throws Exception {
        // 获取总记录数
        long totalRecords = getTotalRecords();
        System.out.println("总记录数: " + totalRecords);

        // 使用单线程顺序处理
        long processed = 0;
        lastId = getMinId(); // 初始化 lastId

        while (processed < totalRecords) {
            int pageProcessed = processPage(lastId, PAGE_SIZE);

            // 如果页面处理0条记录，提前退出
            if (pageProcessed == 0) {
                System.out.println("没有更多记录可处理");
                break;
            }

            processed += pageProcessed;
            System.out.printf("进度: %.1f%% (%d/%d)%n",
                    processed * 100.0 / totalRecords, processed, totalRecords);
        }

        System.out.println("迁移完成! 共处理 " + processed + " 条记录");
    }

    private static int processPage(long startId, int pageSize) {
        int processed = 0;
        long currentMaxId = startId; // 跟踪当前页的最大ID

        try (Connection conn = dataSource.getConnection()) {
            // 1. 查询当前页数据
            String selectSQL = "SELECT id, novel_id, title, chapter_number, content, " +
                    "is_deleted, created_at, updated_at, own_photo, true_id " +
                    "FROM chapter WHERE id >= ? ORDER BY id LIMIT ?";

            try (PreparedStatement select = conn.prepareStatement(selectSQL)) {
                select.setLong(1, startId);
                select.setInt(2, pageSize);

                try (ResultSet rs = select.executeQuery()) {
                    // 2. 准备批量插入
                    String insertSQL = "INSERT INTO chapter_copy VALUES (?,?,?,?,?,?,?,?,?,?)";
                    try (PreparedStatement insert = conn.prepareStatement(insertSQL)) {
                        conn.setAutoCommit(false);

                        int batchCount = 0;
                        while (rs.next()) {
                            long id = rs.getLong("id");
                            currentMaxId = id; // 更新当前最大ID

                            // 读取数据
                            long novelId = rs.getLong("novel_id");
                            String title = rs.getString("title");
                            int chapterNum = rs.getInt("chapter_number");
                            String content = rs.getString("content");
                            boolean isDeleted = rs.getBoolean("is_deleted");
                            Timestamp createdAt = rs.getTimestamp("created_at");
                            Timestamp updatedAt = rs.getTimestamp("updated_at");
                            boolean ownPhoto = rs.getBoolean("own_photo");
                            String trueId = rs.getString("true_id");

                            // 流式压缩内容
                            byte[] compressed = CompressionUtils.compress(content);

                            // 设置插入参数
                            insert.setLong(1, id);
                            insert.setLong(2, novelId);
                            insert.setString(3, title);
                            insert.setInt(4, chapterNum);
                            insert.setBytes(5, compressed);
                            insert.setBoolean(6, isDeleted);
                            insert.setTimestamp(7, createdAt);
                            insert.setTimestamp(8, updatedAt);
                            insert.setBoolean(9, ownPhoto);
                            insert.setString(10, trueId);

                            insert.addBatch();
                            batchCount++;
                            processed++;

                            // 批量提交
                            if (batchCount % BATCH_SIZE == 0) {
                                insert.executeBatch();
                                conn.commit();
                                batchCount = 0;
                            }
                        }

                        // 提交最后一批
                        if (batchCount > 0) {
                            insert.executeBatch();
                            conn.commit();
                        }
                    }
                }
            }

            // 更新全局 lastId 为当前页最大ID+1
            lastId = currentMaxId + 1;
        } catch (Exception e) {
            e.printStackTrace();
            // 发生错误时从下一个ID开始
            lastId = currentMaxId + 1;
        }

        return processed;
    }

    // 流式压缩方法（内存优化）
    private static byte[] streamCompress(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        // 对于小内容直接返回
        if (content.length() < 4096) {
            return content.getBytes(StandardCharsets.UTF_8);
        }

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(content.length() / 2);
            try (GZIPOutputStream gzip = new GZIPOutputStream(bos, 8192)) {
                char[] buffer = new char[8192];
                StringReader reader = new StringReader(content);
                int charsRead;

                // 分块读取和压缩
                while ((charsRead = reader.read(buffer)) != -1) {
                    String chunk = new String(buffer, 0, charsRead);
                    gzip.write(chunk.getBytes(StandardCharsets.UTF_8));
                }
            }
            return bos.toByteArray();
        } catch (Exception e) {
            return content.getBytes(StandardCharsets.UTF_8);
        }
    }

    private static long getTotalRecords() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM chapter")) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    private static long getMinId() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MIN(id) FROM chapter")) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    // 获取最大ID（用于调试）
    private static long getMaxId() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM chapter")) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }
}