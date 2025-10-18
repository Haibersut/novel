package com.wtl.novel.siteMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@Component
public class NovelHtmlGenerator {

    private static final Logger log = LoggerFactory.getLogger(NovelHtmlGenerator.class);
    private static final Random RANDOM = new Random();
    
    private final DataSource dataSource;

    public NovelHtmlGenerator(@Qualifier("primaryDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 执行HTML生成任务
     */
    public void execute() {
        log.info("开始执行小说HTML生成任务");
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

            for (Map<String, Object> novel : novels) {
                try {
                    Long novelId = (Long) novel.get("id");
                    String novelTitle = (String) novel.get("title");
                    
                    if (novelId == null) {
                        log.warn("小说ID为空，跳过");
                        continue;
                    }

                    log.debug("处理小说: {} - {}", novelId, novelTitle);

                    // 获取章节信息
                    List<Map<String, Object>> chapters = getChaptersByNovelId(connection, novelId);
                    if (chapters == null) {
                        chapters = new ArrayList<>();
                    }

                    // 获取标签信息
                    List<Map<String, Object>> tags = getTagsByNovelId(connection, novelId);
                    if (tags == null) {
                        tags = new ArrayList<>();
                    }

                    // 获取第一章内容预览
                    String firstChapterPreview = getFirstChapterContentPreview(connection, novelId);
                    if (firstChapterPreview == null || firstChapterPreview.isEmpty()) {
                        firstChapterPreview = "暂无章节内容";
                    }
                    String aaaa = firstChapterPreview.length() > 120 ? firstChapterPreview.substring(0, 120) : firstChapterPreview;
                    
                    // 生成HTML内容
                    String htmlContent = generateHtmlContent(novel, chapters, tags, aaaa, firstChapterPreview);

                    // 保存HTML文件
                    saveHtmlFile(novelId, htmlContent);
                } catch (Exception e) {
                    log.error("处理单个小说时出错: {}", novel.get("id"), e);
                }
            }

            log.info("HTML生成完成！");
        } catch (Exception e) {
            log.error("HTML生成任务执行失败", e);
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

    // 获取小说章节
    private List<Map<String, Object>> getChaptersByNovelId(Connection connection, Long novelId) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT id,chapter_number, title FROM chapter WHERE novel_id = ? AND is_deleted = 0 ORDER BY chapter_number";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, novelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> chapter = new HashMap<>();
                    chapter.put("chapter_number", rs.getInt("chapter_number"));
                    chapter.put("title", rs.getString("title"));
                    chapter.put("id", rs.getString("id"));
                    result.add(chapter);
                }
            }
        }

        return result;
    }

    // 获取小说标签
    private List<Map<String, Object>> getTagsByNovelId(Connection connection, Long novelId) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT t.name, t.true_name FROM tag t " +
                "JOIN novel_tag nt ON t.id = nt.tag_id " +
                "WHERE nt.novel_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, novelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> tag = new HashMap<>();
                    tag.put("name", rs.getString("name"));
                    tag.put("true_name", rs.getString("true_name"));
                    result.add(tag);
                }
            }
        }

        return result;
    }

    // 获取小说第一章内容预览（前100个字符）
    private String getFirstChapterContentPreview(Connection connection, Long novelId) throws SQLException {
        String sql = "SELECT content FROM chapter WHERE novel_id = ? AND chapter_number = 1 AND is_deleted = 0";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, novelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String content = rs.getString("content");
                    if (content != null) {
                        // 截取前100个字符
                        return content;
                    }
                }
            }
        }

        return "暂无章节内容";
    }

    // 生成HTML内容
    private String generateHtmlContent(Map<String, Object> novel,
                                              List<Map<String, Object>> chapters,
                                              List<Map<String, Object>> tags,
                                              String firstChapterPreview, String content) {
        StringBuilder html = new StringBuilder();

        // HTML头部
        html.append("<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n");
        html.append("  <meta charset=\"UTF-8\" />\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n");
        html.append("  <meta name=\"description\" content=\"").append(novel.get("title")).append("在汉化小说网，您可以免费在线阅读最新的韩文轻小说的高质量汉化版，全网更新最快、种类最全，支持手机、电脑多端浏览，立即开始精彩阅读！").append("\">\n");
        html.append("  <title>").append("译名:").append(novel.get("title")).append("汉化小说网 - 韩版轻小说汉化 | 日版轻小说汉化 | 韩文小说 | 日文小说").append("</title>\n");

        // 样式部分
        html.append("<style>\n");
        html.append(".novel-container {\n");
        html.append("  width: 100%;\n");
        html.append("  background-color: #f5f5f5;\n");
        html.append("  min-height: 100vh;\n");
        html.append("  padding-bottom: 20px;\n");
        html.append("}\n");

        html.append(".content-wrapper {\n");
        html.append("  max-width: 60%;\n");
        html.append("  margin: 0 auto;\n");
        html.append("  padding: 0 15px;\n");
        html.append("}\n");

        html.append(".main-content {\n");
        html.append("  padding-top: 15px;\n");
        html.append("}\n");

        html.append(".novel-header {\n");
        html.append("  background-color: #fff;\n");
        html.append("  border-radius: 10px;\n");
        html.append("  overflow: hidden;\n");
        html.append("  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);\n");
        html.append("  margin-bottom: 20px;\n");
        html.append("}\n");
        html.append(".section-title1 {font-size: 18px;font-weight: 500;color: #333;white-space: pre-line;word-wrap: break-word;line-height: 1.5;}\n");

        html.append(".cover {\n");
        html.append("  width: 120px;\n");
        html.append("  height: 160px;\n");
        html.append("  overflow: hidden;\n");
        html.append("}\n");

        html.append(".cover img {\n");
        html.append("  width: 100%;\n");
        html.append("  height: 100%;\n");
        html.append("  object-fit: cover;\n");
        html.append("}\n");

        html.append(".novel-info {\n");
        html.append("  padding: 15px;\n");
        html.append("  position: relative;\n");
        html.append("}\n");

        html.append(".novel-title {\n");
        html.append("  font-size: 18px;\n");
        html.append("  font-weight: bold;\n");
        html.append("  margin-bottom: 10px;\n");
        html.append("  color: #333;\n");
        html.append("}\n");

        html.append(".tags {\n");
        html.append("  display: flex;\n");
        html.append("  flex-wrap: wrap;\n");
        html.append("  margin-bottom: 10px;\n");
        html.append("}\n");

        html.append(".tag {\n");
        html.append("  background-color: #f0f0f0;\n");
        html.append("  color: #666;\n");
        html.append("  padding: 2px 8px;\n");
        html.append("  border-radius: 10px;\n");
        html.append("  font-size: 12px;\n");
        html.append("  margin-right: 8px;\n");
        html.append("  margin-bottom: 5px;\n");
        html.append("}\n");

        html.append(".rating {\n");
        html.append("  display: flex;\n");
        html.append("  align-items: center;\n");
        html.append("  margin-bottom: 15px;\n");
        html.append("}\n");

        html.append(".score {\n");
        html.append("  font-size: 24px;\n");
        html.append("  font-weight: bold;\n");
        html.append("  color: #ff6b00;\n");
        html.append("  margin-right: 5px;\n");
        html.append("}\n");

        html.append(".rating-count {\n");
        html.append("  font-size: 14px;\n");
        html.append("  color: #999;\n");
        html.append("}\n");

        html.append(".novel-stats {\n");
        html.append("  display: flex;\n");
        html.append("  font-size: 12px;\n");
        html.append("  color: #999;\n");
        html.append("  margin-bottom: 15px;\n");
        html.append("}\n");

        html.append(".stat {\n");
        html.append("  margin-right: 15px;\n");
        html.append("}\n");

        html.append(".action-buttons {\n");
        html.append("  position: absolute;\n");
        html.append("  bottom: 15px;\n");
        html.append("  right: 15px;\n");
        html.append("  display: flex;\n");
        html.append("}\n");

        html.append(".read-button {\n");
        html.append("  background-color: #ff6b00;\n");
        html.append("  color: white;\n");
        html.append("  border: none;\n");
        html.append("  border-radius: 20px;\n");
        html.append("  padding: 8px 15px;\n");
        html.append("  font-size: 14px;\n");
        html.append("  margin-right: 10px;\n");
        html.append("}\n");

        html.append(".shelf-button, .recommend-button {\n");
        html.append("  background-color: #f5f5f5;\n");
        html.append("  color: #666;\n");
        html.append("  border: none;\n");
        html.append("  border-radius: 20px;\n");
        html.append("  padding: 8px 15px;\n");
        html.append("  font-size: 14px;\n");
        html.append("}\n");

        html.append(".section {\n");
        html.append("  background-color: #fff;\n");
        html.append("  border-radius: 10px;\n");
        html.append("  padding: 15px;\n");
        html.append("  margin-bottom: 20px;\n");
        html.append("  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);\n");
        html.append("}\n");

        html.append(".section-title {\n");
        html.append("  font-size: 18px;\n");
        html.append("  font-weight: bold;\n");
        html.append("  margin-bottom: 15px;\n");
        html.append("  color: #333;\n");
        html.append("  border-left: 4px solid #ff6b00;\n");
        html.append("  padding-left: 10px;\n");
        html.append("}\n");

        html.append(".chapter-list1 {\n");
        html.append("  overflow-y: auto;\n");
        html.append("}\n");

        html.append(".chapter-grid {\n");
        html.append("  display: grid;\n");
        html.append("  grid-template-columns: repeat(3, 1fr);\n");
        html.append("  gap: 10px;\n");
        html.append("}\n");

        html.append(".chapter-item {\n");
        html.append("  display: flex;\n");
        html.append("  flex-direction: column;\n");
        html.append("  padding: 10px;\n");
        html.append("  background-color: #f9f9f9;\n");
        html.append("  border-radius: 5px;\n");
        html.append("  font-size: 12px;\n");
        html.append("  color: #333;\n");
        html.append("}\n");

        html.append(".chapter-number {\n");
        html.append("  color: #ff6b00;\n");
        html.append("  font-weight: bold;\n");
        html.append("  margin-bottom: 5px;\n");
        html.append("}\n");

        html.append(".chapter-title {\n");
        html.append("  font-size: 12px;\n");
        html.append("  color: #666;\n");
        html.append("  overflow: hidden;\n");
        html.append("  text-overflow: ellipsis;\n");
        html.append("  white-space: nowrap;\n");
        html.append("}\n");

        html.append("@media (max-width: 768px) {\n");
        html.append("  .content-wrapper {\n");
        html.append("    max-width: 100%;\n");
        html.append("    padding: 0 10px;\n");
        html.append("  }\n");

        html.append("  .novel-header {\n");
        html.append("    flex-direction: column;\n");
        html.append("  }\n");

        html.append("  .cover {\n");
        html.append("    width: 100%;\n");
        html.append("    height: 200px;\n");
        html.append("    margin-bottom: 10px;\n");
        html.append("  }\n");

        html.append("  .novel-info {\n");
        html.append("    padding: 10px;\n");
        html.append("  }\n");

        html.append("  .chapter-grid {\n");
        html.append("    grid-template-columns: repeat(2, 1fr);\n");
        html.append("  }\n");
        html.append("}\n");
        html.append("</style>\n");

        html.append("</head>\n<body>\n");

        // 主体内容
        html.append("  <div class=\"novel-container\">\n");
        html.append("    <div class=\"content-wrapper\">\n");
        html.append("      <div class=\"main-content\">\n");
        html.append("        <div class=\"novel-header\">\n");
        html.append("          <div class=\"novel-info\">\n");
        html.append("            <h2 class=\"novel-title\">").append(novel.get("title")).append("</h2>\n");
        html.append("            <h1 class=\"novel-title\">").append("第一章内容预览：").append(firstChapterPreview).append("......").append("</h1>\n");

        // 标签
        html.append("            <div class=\"tags\">\n");
        for (Map<String, Object> tag : tags) {
            html.append("              <span class=\"tag\">").append(tag.get("name")).append("</span>\n");
        }
        html.append("            </div>\n");

        // 评分和统计
        html.append("            <div class=\"rating\">\n");
        html.append("              <span class=\"score\">").append(getRandomWordCount()).append("</span>\n");
        html.append("              <span class=\"rating-count\">收藏</span>\n");
        html.append("            </div>\n");

        html.append("            <div class=\"novel-stats\">\n");
        html.append("              <span class=\"stat\">").append(getRandomWordCount()).append("万字</span>\n");
        html.append("            </div>\n");

        // 操作按钮
        html.append("            <div class=\"action-buttons\">\n");
        html.append("              <button class=\"read-button\" onclick=\"window.location.href='/novelDetail/").append(novel.get("id")).append("'\">阅读</button>\n");
        html.append("            </div>\n");

        html.append("          </div>\n");
        html.append("        </div>\n");

        html.append("<div class=\"section\"><h4>前言：</h4><div class=\"section-title1\">");
        html.append(content);
        html.append("</div></div><div class=\"section\">");

        // 章节列表
        html.append("        <div class=\"section\">\n");
        html.append("          <h2 class=\"section-title\">目录 · ").append(chapters.size()).append("章</h2>\n");
        html.append("          <div class=\"chapter-list1\">\n");
        html.append("            <div class=\"chapter-grid\">\n");

        for (Map<String, Object> chapter : chapters) {
            html.append("              <div onclick=\"window.location.href='/chapterDetail/").append(chapter.get("id")).append("'\" ").append("class=\"chapter-item\">\n");
            html.append("                <div class=\"chapter-number\">第").append(chapter.get("chapter_number")).append("章</div>\n");
            html.append("                <div class=\"chapter-title\">").append(chapter.get("title")).append("</div>\n");
            html.append("              </div>\n");
        }

        html.append("            </div>\n");
        html.append("          </div>\n");
        html.append("        </div>\n");

        html.append("      </div>\n");
        html.append("    </div>\n");
        html.append("  </div>\n");
        html.append("</div></body>\n</html>");

        return html.toString();
    }

    // 保存HTML文件
    private void saveHtmlFile(Long novelId, String htmlContent) {
        try {
            File file = new File("C:\\Users\\30402\\IdeaProjects\\novel\\src\\main\\java\\com\\wtl\\novel\\siteMap\\model\\novelDetail" + novelId + ".html");
            try (FileWriter fw = new FileWriter(file);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(htmlContent);
            }
            log.debug("已生成: {}.html", novelId);
        } catch (IOException e) {
            log.error("保存HTML文件失败: {}", novelId, e);
        }
    }

    // 生成随机字数
    private String getRandomWordCount() {
        return String.valueOf(10 + RANDOM.nextInt(90));
    }
}