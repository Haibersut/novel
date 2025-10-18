package com.wtl.novel.siteMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.io.*;

@Component
public class NovelIndexGenerator {

    private static final Logger log = LoggerFactory.getLogger(NovelIndexGenerator.class);
    
    private final DataSource dataSource;

    public NovelIndexGenerator(@Qualifier("primaryDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 执行索引页面生成任务
     */
    public void execute() {
        log.info("开始执行小说索引页面生成任务");
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            if (connection == null) {
                log.error("无法获取数据库连接");
                return;
            }

            List<Map<String, Object>> novels = getNovels(connection);
            if (novels == null || novels.isEmpty()) {
                log.warn("没有找到小说数据");
                return;
            }

            String htmlContent = generateIndexHtmlContent(novels);

            saveHtmlFile("index.html", htmlContent);

            log.info("索引页面生成完成！");
        } catch (Exception e) {
            log.error("索引页面生成任务执行失败", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("关闭数据库连接失败", e);
                }
            }
        }
    }

    // 获取所有小说信息
    private List<Map<String, Object>> getNovels(Connection connection) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT id, title, true_name FROM novel WHERE is_deleted = 0";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> novel = new HashMap<>();
                novel.put("id", rs.getLong("id"));
                novel.put("title", rs.getString("title"));
                novel.put("true_name", rs.getString("true_name"));
                result.add(novel);
            }
        }

        return result;
    }

    // 生成索引页面HTML内容
    private String generateIndexHtmlContent(List<Map<String, Object>> novels) {
        StringBuilder html = new StringBuilder();

        // HTML头部
        html.append("<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n");
        html.append("  <meta charset=\"UTF-8\" />\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n");
        html.append("  <meta name=\"description\" content=\"在汉化小说网，您可以免费在线阅读最新的韩文轻小说的高质量汉化版，全网更新最快、种类最全，支持手机、电脑多端浏览，立即开始精彩阅读！\">\n");
        html.append("  <title>在汉化小说网，您可以免费在线阅读最新的韩文轻小说的高质量汉化版，全网更新最快、种类最全，支持手机、电脑多端浏览，立即开始精彩阅读！的索引页</title>\n");
        html.append("</head>\n<body>\n");

        // 主体内容
        html.append("<div>\n");
        html.append("  <h1>在汉化小说网，您可以免费在线阅读最新的韩文轻小说的高质量汉化版，全网更新最快、种类最全，支持手机、电脑多端浏览，立即开始精彩阅读！</h1>\n");

        // 生成小说链接列表
        for (Map<String, Object> novel : novels) {
            Long novelId = (Long) novel.get("id");
            String novelTitle = (String) novel.get("title");
            String novelTrueTitle = (String) novel.get("true_name");

            html.append("  <a href=\"/model/").append("novelDetail").append(novelId).append(".html\">原名：").append(novelTrueTitle)
                    .append("，译名：").append(novelTitle).append("</a><br>\n");
        }

        html.append("</div>\n");
        html.append("</body>\n</html>");

        return html.toString();
    }

    // 保存HTML文件
    private void saveHtmlFile(String fileName, String htmlContent) {
        try {
            File file = new File("C:\\Users\\30402\\IdeaProjects\\novel\\src\\main\\java\\com\\wtl\\novel\\siteMap\\model\\" + fileName);
            try (FileWriter fw = new FileWriter(file);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(htmlContent);
            }
            log.debug("已生成: {}", fileName);
        } catch (IOException e) {
            log.error("保存HTML文件失败: {}", fileName, e);
        }
    }
}