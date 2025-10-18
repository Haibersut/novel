package com.wtl.novel.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NovelpiaCrawler {

    private static final Logger log = LoggerFactory.getLogger(NovelpiaCrawler.class);

    /* ========== 输出文件 ========== */
    private static final Path OUTPUT_FILE = Path.of("novelpia_result.csv");

    /* ========== 线程池 ========== */
    private static final int THREADS = 10;

    /* ========== HttpClient 连接池 ========== */
    private static final PoolingHttpClientConnectionManager CM = new PoolingHttpClientConnectionManager();
    static {
        CM.setMaxTotal(THREADS);
        CM.setDefaultMaxPerRoute(THREADS);
    }

    private static final RequestConfig REQ_CONFIG = RequestConfig.custom().build();

    private static final CloseableHttpClient HTTP_CLIENT = HttpClients.custom()
            .setConnectionManager(CM)
            .setDefaultRequestConfig(REQ_CONFIG)
            .build();

    /* ========== 实时写锁 ========== */
    private static final Object WRITE_LOCK = new Object();

    private final DataSource dataSource;

    public NovelpiaCrawler(@Qualifier("primaryDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 执行爬取任务
     */
    public void execute() {
        log.info("开始执行Novelpia爬取任务");
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);

        try {
            // 保证文件存在
            if (Files.notExists(OUTPUT_FILE)) {
                Files.createFile(OUTPUT_FILE);
            }

            String sql = "SELECT id, true_id FROM novel WHERE is_deleted = 0";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    long id       = rs.getLong("id");
                    String trueId = rs.getString("true_id");
                    pool.submit(() -> {
                        try {
                            Record r = crawlOne(id, trueId);
                            synchronized (WRITE_LOCK) {
                                Files.writeString(OUTPUT_FILE,
                                        r.novelId + "&" + r.like + "&" + r.read + "&" + r.authorId + "&" + r.authorName + System.lineSeparator(),
                                        StandardOpenOption.APPEND);
                            }
                            log.debug("完成处理小说: {}", id);
                        } catch (Exception e) {
                            log.error("爬取失败 true_id={}", trueId, e);
                        }
                    });
                }
            }

            pool.shutdown();
            if (!pool.awaitTermination(1, TimeUnit.HOURS)) {
                log.warn("爬取任务超时,强制关闭线程池");
                pool.shutdownNow();
            }
            log.info("全部完成，结果已实时写入 {}", OUTPUT_FILE.toAbsolutePath());
        } catch (Exception e) {
            log.error("Novelpia爬取任务执行失败", e);
            pool.shutdownNow();
        }
    }

    /* ========== 抓取 & 解析 ========== */
    private static Record crawlOne(long id, String trueId) throws Exception {
        String novelId  = String.valueOf(id);
        String html = sendGet("https://novelpia.com/novel/" + trueId);
        String read   = extractReadText(html);
        String like   = extractLikeText(html);
        String authorId  = "";
        String authorName = "";

        Document doc = Jsoup.parse(html);
        Element a = doc.selectFirst("a.writer-name");
        if (a != null) {
            authorName = a.text().trim();
            String href = a.attr("href"); // /user/12345
            Matcher m = Pattern.compile("/user/(\\d+)").matcher(href);
            if (m.find()) {
                authorId = m.group(1);
            }
        }
        return new Record(novelId, like, read, authorId, authorName);
    }

    /* ========== HTTP 请求 ========== */
    private static String sendGet(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        get.setHeader("User-Agent", "Mozilla/5.0");
        try (CloseableHttpResponse resp = HTTP_CLIENT.execute(get)) {
            return new String(resp.getEntity().getContent().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    /* ========== 解析逻辑 ========== */
    private static String extractReadText(String html) {
        if (isInvalid(html)) return "0";
        Document doc = Jsoup.parse(html);
        Element span = doc.selectFirst("div.counter-line-a > p > span:nth-of-type(2)");
        return span == null ? "0" : span.text().trim();
    }

    private static String extractLikeText(String html) {
        if (isInvalid(html)) return "0";
        Document doc = Jsoup.parse(html);
        Element p = doc.selectFirst("div.icon-btn-area a.btn-like > p.bottom-txt");
        return p == null ? "0" : p.text().trim();
    }

    private static boolean isInvalid(String html) {
        return html == null || html.isBlank() || !html.contains("counter-line-a");
    }

    /* ========== 内部数据类 ========== */
    private static class Record {
        final String novelId, like, read, authorId, authorName;
        Record(String novelId, String like, String read, String authorId, String authorName) {
            this.novelId = novelId;
            this.like = like;
            this.read = read;
            this.authorId = authorId;
            this.authorName = authorName;
        }
    }
}