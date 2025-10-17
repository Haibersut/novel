package com.wtl.novel.translator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtl.novel.CDO.SyosetuNovelDetail;
import com.wtl.novel.entity.*;
import com.wtl.novel.repository.*;
import com.wtl.novel.util.RemoveRepeatUtil;
import okhttp3.*;
import okio.BufferedSource;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


// 成为小说家
@Component
public class Syosetu {

    Set<String> executeId = ConcurrentHashMap.newKeySet();

    private static final String proxyHost = "127.0.0.1"; // 替换为你的代理IP
    private static final int proxyPort = 7890; // 替换为你的代理端口
    private static final int poolSize = Runtime.getRuntime().availableProcessors() * 60;

    // 创建代理对象
//    private static final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
    @Autowired
    private ChapterErrorExecuteRepository chapterErrorExecuteRepository;
    @Autowired
    private DictionaryRepository dictionaryRepository;
    @Autowired
    private UserFeedbackRepository userFeedbackRepository;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private NovelTagRepository novelTagRepository;

    @Autowired
    private PlatformApiKeyRepository platformApiKeyRepository;
    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ChapterExecuteRepository chapterExecuteRepository;

    private static final AtomicBoolean downloaded = new AtomicBoolean(true);
    private static final AtomicBoolean translation = new AtomicBoolean(true);
    private static final AtomicBoolean executeError = new AtomicBoolean(true);

//    @Scheduled(cron = "0 15 0/3 * * ?")
    public void executeTask1() {
        final boolean executeSyosetuDownload = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeSyosetuDownload").getValueField());
        if (executeSyosetuDownload) {
            if (downloaded.compareAndSet(true, false)) {
                try {
                    executeDownload();
                    System.out.println("定时任务执行: " + LocalDateTime.now());
                } finally {
                    downloaded.set(true);
                }
            }
        }
    }

//    @Scheduled(cron = "0 45 0/3 * * ?")
    public void executeTask2() {
        final boolean executeSyosetuTr = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeSyosetuTr").getValueField());
        if (executeSyosetuTr) {
            if (translation.compareAndSet(true, false)) {
                try {
                    List<Novel> novels = getNovels();
                    List<Long> novelIdList = novels.stream().map(Novel::getId).toList();
                    List<ChapterExecute> chapters = chapterExecuteRepository.findByNovelIdsAndNowStateAndIsDeletedFalse(novelIdList,0);
                    List<ChapterExecute> chapterExecutes = executeTranslation("siliconflow", chapters);
                    List<ChapterExecute> chaptersException = chapterExecuteRepository.findByNovelIdsAndNowStateAndIsDeletedFalse(novelIdList, 1);
                    executeTranslationException("siliconflow", chaptersException);
                    // 你的任务逻辑
                    System.out.println("定时任务执行: " + LocalDateTime.now());
                } finally {
                    translation.set(true);
                }
            }
        }
    }

    // 搜索功能
    public String searchWeb(String keyword) {
        String trim = keyword.trim();
        if (trim.isEmpty()) {
            return "";
        }
        String syosetuSearchUrl = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("syosetuSearchUrl").getValueField();
        try {
            Document document = Jsoup.connect(String.format(syosetuSearchUrl, trim))
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10000)
//                    .proxy(proxy)
                    .get();
            Elements elements = document.select("div.searchkekka_box");
            return elements.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 保存小说全部信息
    public String saveNovel(String novelTrueId, SyosetuNovelDetail syosetuNovelDetail, Novel save) {
        try {
            executeId.add(novelTrueId);
            final String epFormat = "EP%s";
            Long novelId = save.getId();
            List<String> tagList = syosetuNovelDetail.getTagList();
            for (String tag : tagList) {
                Tag tagToSave = new Tag(tag, "syosetu", tag);
                Optional<Tag> existingTag = tagRepository.findByTrueName(tagToSave.getTrueName());
                if (existingTag.isEmpty()) {
                    Tag syosetuTag = tagRepository.save(tagToSave);
                    novelTagRepository.save(new NovelTag(novelId, syosetuTag.getId()));
                } else {
                    Tag tag1 = existingTag.get();
                    novelTagRepository.save(new NovelTag(novelId, tag1.getId()));
                }
            }
            String episodeFile = String.format(epFormat, String.format("%04d", 0));
            ChapterExecute chapter = new ChapterExecute(
                    novelId,
                    episodeFile,
                    0,
                    syosetuNovelDetail.getPrologue(),
                    0,
                    "0",
                    false
            );
            chapterExecuteRepository.save(chapter);
            String novelType = syosetuNovelDetail.getNovelType();
            if (novelType.equals("短編")) {
                // 对于短篇trueId=0代表序言，trueId=1代表内容
                String shortNovel = getShortNovel(novelTrueId);
                String chapterNum = String.format(epFormat, String.format("%04d", 1));
                ChapterExecute chapterExecute = new ChapterExecute(
                        novelId,
                        chapterNum,
                        1,
                        shortNovel,
                        0,
                        "1",
                        false
                );
                chapterExecuteRepository.save(chapterExecute);
            } else {
                Map<Integer, String> allChapter = getAllChapter(novelTrueId);
                List<ChapterExecute> chapterExecuteList = new ArrayList<>();
                allChapter.forEach((k, v) -> {
                    String chapterNum = String.format(epFormat, String.format("%04d", k));
                    ChapterExecute chapterExecute = new ChapterExecute(
                            novelId,
                            chapterNum,
                            k,
                            v,
                            0,
                            String.valueOf(k),
                            false
                    );
                    chapterExecuteList.add(chapterExecute);
                });
                chapterExecuteRepository.saveAll(chapterExecuteList);
            }
            return "已收录本小说，请耐心等待<定时汉化任务>执行，这可能需要几个小时的时间：{"+ novelId +"}";
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executeId.remove(novelTrueId);
        }
    }

    public String getShortNovel(String novelTrueId) {
        try {
            StringBuilder novelContent = new StringBuilder();
            String syosetuSavePrologueUrl = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("syosetuSavePrologueUrl").getValueField();
            Document document = Jsoup.connect(String.format(syosetuSavePrologueUrl, novelTrueId))
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10000)
//                    .proxy(proxy)
                    .get();
            // 根据ID获取元素
            Elements elementsByClass = document.getElementsByClass("p-novel__body");
            Element novelEx = elementsByClass.get(0);
            if (novelEx != null) {
                // 获取HTML内容
                String htmlContent = novelEx.html();
                // 将<br>标签替换为换行符
                String textWithNewlines = htmlContent.replaceAll("<br\\s*/?>", "\n");
                // 移除<font>标签的属性，但保留标签
                textWithNewlines = textWithNewlines.replaceAll("<font[^>]*>", "<font>");
                // 替换<font>标签为换行符
                textWithNewlines = textWithNewlines.replaceAll("<font>", "\n");
                // 移除</font>标签
                textWithNewlines = textWithNewlines.replaceAll("</font>", "");
                // 移除HTML标签
                textWithNewlines = textWithNewlines.replaceAll("<[^>]+>", "");

                // 按换行符分割文本，逐行处理并输出
                String[] lines = textWithNewlines.split("\n");
                for (String line : lines) {
                    String trimmedLine = line.trim();
                    if (!trimmedLine.isEmpty()) {
                        System.out.println(trimmedLine);
                        novelContent.append(trimmedLine).append("\n");
                    }
                }
            } else {
                System.out.println("未找到ID为novel_ex的元素");
            }
            return novelContent.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 获取标签列表
    public SyosetuNovelDetail saveNovelDetail(String novelTrueId) throws Exception {
        StringBuilder prologue = new StringBuilder();
        List<String> tagList = new ArrayList<>();
        String title = "";
        String novelType = "";
        String syosetuNovelDetail = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("syosetuNovelDetail").getValueField();
        // 使用Jsoup发送GET请求并解析HTML
        Document document = Jsoup.connect(String.format(syosetuNovelDetail, novelTrueId))
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(10000)
//                .proxy(proxy)
                .get();

        // 1. 获取あらすじ的下一个标签
        Element arasujiDt = document.select("dt:contains(あらすじ)").first();
        if (arasujiDt != null) {
            Element arasujiDd = arasujiDt.nextElementSibling();
            if (arasujiDd != null) {
                // 获取HTML内容
                String htmlContent = arasujiDd.html();
                // 将<br>标签替换为换行符
                String textWithNewlines = htmlContent.replaceAll("<br\\s*/?>", "\n");
                // 移除<font>标签的属性，但保留标签
                textWithNewlines = textWithNewlines.replaceAll("<font[^>]*>", "<font>");
                // 替换<font>标签为换行符
                textWithNewlines = textWithNewlines.replaceAll("<font>", "\n");
                // 移除</font>标签
                textWithNewlines = textWithNewlines.replaceAll("</font>", "");
                // 移除HTML标签
                textWithNewlines = textWithNewlines.replaceAll("<[^>]+>", "");

                // 按换行符分割文本，逐行处理并输出
                String[] lines = textWithNewlines.split("\n");
                for (String line : lines) {
                    String trimmedLine = line.trim();
                    if (!trimmedLine.isEmpty()) {
                        prologue.append(trimmedLine).append("\n");
                    }
                }
            }
        }

        // 2. 获取キーワード的下一个标签
        Element keywordDt = document.select("dt:contains(キーワード)").first();
        if (keywordDt != null) {
            Element keywordDd = keywordDt.nextElementSibling();
            if (keywordDd != null) {
                tagList.addAll(Arrays.asList(keywordDd.text().trim().split(" ")));
            }
        }

        // 3. 获取<h1 class="p-infotop-title">标签中的内容
        Element titleElement = document.select("h1.p-infotop-title").first();
        if (titleElement != null) {
            title = titleElement.text().trim();
        }

        // 4.获取小说状态（完结、连载、短篇）
        Element novelTypeElement = document.select("span.p-infotop-type__type").first();
        if (novelTypeElement != null) {
            novelType = novelTypeElement.text().trim();
        }

        System.out.println(1);
        System.out.println(tagList);
        return new SyosetuNovelDetail(prologue.toString(),title,novelTrueId,tagList,novelType);
    }


    public Map<Integer, String> getAllChapter(String novelTrueId) {
        String syosetuGetAllChapter = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("syosetuGetAllChapter").getValueField();
        String oneUrl = String.format(syosetuGetAllChapter, novelTrueId, 1);
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        int maxRetries = 3; // 最大重试次数
        int timeout = 30000; // 超时时间（30秒）
        try {
            // 获取总章节数
            int totalChapters = getTotalChapters(oneUrl, timeout, maxRetries);
            if (totalChapters <= 0) {
                return null;
            }

            // 解析每个章节
            for (int i = 1; i <= totalChapters; i++) {
                String chapterUrl = String.format(syosetuGetAllChapter ,novelTrueId, i);
                StringBuilder chapterContent = new StringBuilder();

                for (int retry = 0; retry < maxRetries; retry++) {
                    try {
                        Document chapterDoc = Jsoup.connect(chapterUrl)
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                                .timeout(timeout)
//                                .proxy(proxy)
                                .get();

                        // 获取标题
                        Elements titleElements = chapterDoc.getElementsByClass("p-novel__title");
                        for (Element element : titleElements) {
                            chapterContent.append(element.text()).append("\n");
                        }

                        // 获取正文内容
                        Elements bodyElements = chapterDoc.getElementsByClass("p-novel__body");
                        for (Element element : bodyElements) {
                            String htmlContent = element.html();
                            String textWithNewlines = htmlContent.replaceAll("<br\\s*/?>", "\n");
                            textWithNewlines = textWithNewlines.replaceAll("<[^>]+>", "");
                            String[] lines = textWithNewlines.split("\n");
                            for (String line : lines) {
                                String trimmedLine = line.trim();
                                if (!trimmedLine.isEmpty()) {
                                    chapterContent.append(trimmedLine).append("\n");
                                }
                            }
                        }

                        map.put(i, chapterContent.toString());
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<Integer, String> getAllChapterByChapterList(String novelTrueId, List<Integer> chapterIdList) {
        String syosetuGetAllChapter = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("syosetuGetAllChapter").getValueField();
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        int maxRetries = 3; // 最大重试次数
        int timeout = 30000; // 超时时间（30秒）
        try {

            for (Integer i : chapterIdList) {
                String chapterUrl = String.format(syosetuGetAllChapter ,novelTrueId, i);
                StringBuilder chapterContent = new StringBuilder();

                // 重试机制
                boolean success = false;
                for (int retry = 0; retry < maxRetries; retry++) {
                    try {
                        Document chapterDoc = Jsoup.connect(chapterUrl)
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                                .timeout(timeout)
//                                .proxy(proxy)
                                .get();

                        // 获取标题
                        Elements titleElements = chapterDoc.getElementsByClass("p-novel__title");
                        for (Element element : titleElements) {
                            chapterContent.append(element.text()).append("\n");
                        }

                        // 获取正文内容
                        Elements bodyElements = chapterDoc.getElementsByClass("p-novel__body");
                        for (Element element : bodyElements) {
                            String htmlContent = element.html();
                            String textWithNewlines = htmlContent.replaceAll("<br\\s*/?>", "\n");
                            textWithNewlines = textWithNewlines.replaceAll("<[^>]+>", "");
                            String[] lines = textWithNewlines.split("\n");
                            for (String line : lines) {
                                String trimmedLine = line.trim();
                                if (!trimmedLine.isEmpty()) {
                                    chapterContent.append(trimmedLine).append("\n");
                                }
                            }
                        }

                        map.put(i, chapterContent.toString());
                        success = true;
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    // 获取总章节数
    private static int getTotalChapters(String url, int timeout, int maxRetries) throws IOException {
        boolean success = false;
        Document document = null;
        int retryCount = 0;

        while (!success && retryCount < maxRetries) {
            try {
                document = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(timeout)
//                        .proxy(proxy)
                        .get();
                success = true;
            } catch (IOException e) {
                retryCount++;
                System.out.println("获取总章节数失败，正在重试 (" + retryCount + "/" + maxRetries + ")...");
            }
        }

        if (document == null) {
            return -1;
        }

        Elements numberElements = document.getElementsByClass("p-novel__number");
        for (Element element : numberElements) {
            String text = element.text();
            Pattern pattern = Pattern.compile("/(\\d+)");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return -1;
    }

    public List<ChapterExecute> executeTranslation(String platformName, List<ChapterExecute> chapterExecuteList) {
        // 初始化配置信息（保持单线程获取）
        String siliconflowApiUrl = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflow").getValueField();
        String siliconflowModel = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflowModel_1").getValueField();
        String aiPrompt = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("aiPrompt").getValueField();
        String siliconflowMaxLength = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflowMaxLength").getValueField();
        Platform platform = platformRepository.findPlatformByPlatformName(platformName);
        List<PlatformApiKey> apiKeys = platformApiKeyRepository.findByPlatformIdAndIsDeletedFalse(platform.getId());

        // 创建线程池（建议根据实际情况配置参数）
        int poolSize = 100;
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        // 使用线程安全集合保存结果
        List<ChapterExecute> chapterExecuteOverList = Collections.synchronizedList(new ArrayList<>());
        OkHttpClient httpClient = createOKHttpClient();
        try {
            // 并行处理每个章节
            List<CompletableFuture<Void>> futures = chapterExecuteList.stream()
                    .filter(chapter -> chapter.getNowState() != 3)
                    .map(chapter -> CompletableFuture.runAsync(() ->
                                    processChapter(chapter, apiKeys, siliconflowApiUrl,
                                            siliconflowModel, siliconflowMaxLength, chapterExecuteOverList,aiPrompt, httpClient),
                            executor))
                    .toList();

            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 销毁 OkHttpClient 实例
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
            executor.shutdown();
        }

        return chapterExecuteOverList;
    }

    private static OkHttpClient createOKHttpClient() {
        // 创建连接池（等效于 HttpClient 的连接池配置）
        ConnectionPool connectionPool = new ConnectionPool(
                300, // 最大空闲连接数（相当于 maxTotal）
                5,  // 保持存活时间（单位：分钟）
                TimeUnit.MINUTES
        );

        // 创建 CookieJar（等效于 BasicCookieStore）
        CookieJar cookieJar = new CookieJar() {
            // 实现持久化 Cookie 存储（这里使用内存存储）
            private final Map<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();

            @Override
            public void saveFromResponse(HttpUrl url, @NotNull List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);
            }

            @NotNull
            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                return cookieStore.getOrDefault(url.host(), Collections.emptyList());
            }
        };

        return new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .connectTimeout(120, TimeUnit.SECONDS)     // 连接超时（同 connectionTimeout）
                .readTimeout(120, TimeUnit.SECONDS)        // 读取超时（同 socketTimeout）
                .writeTimeout(120, TimeUnit.SECONDS)       // 写入超时（可视为请求超时）
                .cookieJar(cookieJar)                     // Cookie 管理
                .build();
    }

    private void processChapter(ChapterExecute chapterExecute, List<PlatformApiKey> apiKeys,
                                String apiUrl, String model, String maxLengthStr,
                                List<ChapterExecute> resultList, String aiPrompt, OkHttpClient httpClient) {
        // 每个线程使用独立的随机数生成器
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int maxLength = Integer.parseInt(maxLengthStr);
        StringBuilder okContent = new StringBuilder();
        boolean hasError = false;

        try {
            chapterExecute.setContent(RemoveRepeatUtil.processString(chapterExecute.getContent()));
            // 状态预检查
            if (chapterExecute.getNowState() == 3) return;

            // 选择API Key（线程安全访问）
            PlatformApiKey apiKey = apiKeys.get(random.nextInt(apiKeys.size()));

            // 分割文本
            List<String> textParts = splitTextByLine(chapterExecute.getContent(), maxLength);

            // 更新状态需要加锁或使用乐观锁
            synchronized (chapterExecute) {
                chapterExecute.setNowState(3);
                chapterExecuteRepository.save(chapterExecute);
            }

            // 处理每个文本片段
            for (String part : textParts) {
                part = processMultilineString(part);
                try {
                    String translation = translation(apiKey.getApiKey(), apiUrl, part, model, true, aiPrompt, httpClient);
                    okContent.append(translation);
                } catch (Exception e) {
                    handleAbnormalTranslation(okContent, part);
                    hasError = true;
                    e.printStackTrace();
                }
            }

            // 更新最终状态
            synchronized (chapterExecute) {
                chapterExecute.setTranslatorContent(okContent.toString());
                chapterExecute.setNowState(hasError ? 1 : 2);

                if (hasError) {
                    resultList.add(chapterExecute);
                } else {
                    Chapter chapter = new Chapter();
                    chapter.setChapterNumber(chapterExecute.getChapterNumber());
                    chapter.setContent(chapterExecute.getTranslatorContent());
                    chapter.setTitle(chapterExecute.getTitle());
                    chapter.setNovelId(chapterExecute.getNovelId());
                    chapter.setTrueId(chapterExecute.getTrueId());
                    chapter.setOwnPhoto(chapterExecute.isOwnPhoto());
                    chapterRepository.save(chapter);
                    novelRepository.incrementFontNumberById(chapter.getNovelId(), (long) chapter.getContent().length());
                }


            }
        } catch (Exception e) {
            // 处理全局异常
            synchronized (chapterExecute) {
                chapterExecute.setNowState(1);
                resultList.add(chapterExecute);
            }
            e.printStackTrace();
        }finally {
            chapterExecuteRepository.save(chapterExecute);
        }
    }

    public String translation(String apiKey, String apiUrl, String content, String aiModel, boolean stream, String aiPrompt, OkHttpClient httpClient) throws Exception {
        StringBuilder builder = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();
        content = processMultilineString(content);
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", aiPrompt + content);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiModel);
        requestBody.put("messages", Collections.singletonList(message));
        requestBody.put("stream", stream);

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(RequestBody.create(
                        mapper.writeValueAsString(requestBody),
                        MediaType.parse("application/json")
                ))
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        String last_content = "";
        int repeat_count = 0;
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("apiKey:" + apiKey + "，Unexpected code " + response);
            }

            assert response.body() != null;
            try (BufferedSource source = response.body().source()) {
                while (!source.exhausted()) {
                    String line = source.readUtf8Line();
                    if (line != null && line.startsWith("data: ")) {
                        String jsonStr = line.substring(6).trim();

                        if (jsonStr.equals("[DONE]")) {
                            break;
                        }

                        JsonNode jsonNode = mapper.readTree(jsonStr);
                        JsonNode delta = jsonNode.path("choices")
                                .get(0)
                                .path("delta");

                        if (delta.has("content")) {
                            String text = delta.get("content").asText();
                            if (!text.trim().isEmpty()) {
                                if (content.equals(last_content)) {
                                    repeat_count += 1;
                                } else {
                                    repeat_count = 1;
                                }
                                last_content = text;
                                if (repeat_count > 15) {
                                    source.close();
                                    response.close();
                                    throw new RuntimeException("循环错误！");
                                }
                            }
                            System.out.println(text);
                            builder.append(text);
                        }
                    }
                }
            }
        }
        return builder.toString();
    }


    // 处理单行字符串
    public static String processLine(String line) {
        Pattern pattern = Pattern.compile("(.+?)\\1{3,}");
        Matcher matcher = pattern.matcher(line);
        return matcher.replaceAll("$1$1$1");
    }

    // 封装多行字符串处理逻辑
    public static String processMultilineString(String inputStr) {
        String[] lines = inputStr.split("\n");
        StringBuilder outputSb = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String processedLine = processLine(lines[i]);
            outputSb.append(processedLine);
            if (i < lines.length - 1) {
                outputSb.append("\n"); // 最后一行不添加换行符
            }
        }

        return outputSb.toString();
    }

    private void handleAbnormalTranslation(StringBuilder builder, String original) {
        builder.append("\n下方翻译出现异常！\n")
                .append(original)
                .append("\n上方翻译出现异常！\n");
    }


    public static List<String> splitTextByLine(String text, int maxLength) {
        List<String> chunks = new ArrayList<>();
        String[] lines = text.split("\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String line : lines) {
            // 计算当前块加上新行后的总长度（包括换行符）
            int neededLength = currentChunk.length() + line.length() + 1;
            if (neededLength <= maxLength) {
                currentChunk.append(line).append("\n");
            } else {
                // 将当前块修剪后添加到结果列表
                chunks.add(currentChunk.toString().trim());
                // 重置当前块并添加新行
                currentChunk.setLength(0);
                currentChunk.append(line).append("\n");
            }
        }

        // 处理最后一个块
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    public void executeTranslationException(String platformName, List<ChapterExecute> chapterExecuteList) {
        // 预加载公共配置（线程安全方式）
        String apiUrl = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflow").getValueField();
        String model = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflowModel_1").getValueField();
        String aiPrompt = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("aiPrompt").getValueField();
        Platform platform = platformRepository.findPlatformByPlatformName(platformName);
        List<PlatformApiKey> apiKeys = platformApiKeyRepository.findByPlatformIdAndIsDeletedFalse(platform.getId());

        // 创建线程池（建议根据CPU核心数调整）
        int poolSize = 50;
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        OkHttpClient httpClient = createOKHttpClient();
        // 并行处理每个章节
        try {
            List<CompletableFuture<Void>> futures = chapterExecuteList.stream()
                    .filter(chapter -> chapter.getNowState() != 3)
                    .map(chapter -> CompletableFuture.runAsync(() -> {
                        try {
                            // 处理单个章节
                            ChapterExecute executed = processSingleChapter(chapter, platformName, apiUrl, model, apiKeys, aiPrompt, httpClient);

                            // 保存最终章节
                            if (executed.getNowState() == 2) {
                                saveFinalChapter(executed);
                            }
                        } catch (Exception e) {
                            handleChapterError(chapter, e);
                        }
                    }, executor))
                    .toList();
            // 等待所有任务完成
            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Processing interrupted", e);
            }
        } finally {
            executor.shutdown();
            // 销毁 OkHttpClient 实例
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }

    }

    private synchronized void saveFinalChapter(ChapterExecute executed) {
        Chapter chapter = new Chapter();
        chapter.setChapterNumber(executed.getChapterNumber());
        chapter.setContent(executed.getTranslatorContent());
        chapter.setTitle(executed.getTitle());
        chapter.setNovelId(executed.getNovelId());
        chapter.setTrueId(executed.getTrueId());
        chapter.setOwnPhoto(executed.isOwnPhoto());
        chapterRepository.save(chapter);
        novelRepository.incrementFontNumberById(chapter.getNovelId(), (long) chapter.getContent().length());
    }

    private synchronized void handleChapterError(ChapterExecute chapter, Exception e) {
        chapter.setNowState(1);
        chapterExecuteRepository.save(chapter);
        e.printStackTrace();
    }

    private ChapterExecute processSingleChapter(ChapterExecute chapter, String platformName,
                                                String apiUrl, String model, List<PlatformApiKey> apiKeys, String aiPrompt, OkHttpClient httpClient) {
        // 使用线程安全的随机数
        ThreadLocalRandom random = ThreadLocalRandom.current();
        chapter.setTranslatorContent(chapter.getTranslatorContent().replaceAll("V1Zn[A-Za-z0-9+=]+", ""));
        // 状态检查和初始化
        if (chapter.getNowState() == 3) return chapter;
        Novelpia.ValidationResult result = panduanyichang(chapter.getTranslatorContent());
        try {
            // 更新状态需要同步
            synchronized (chapter) {
                chapter.setNowState(3);
                chapterExecuteRepository.save(chapter);
            }

            // 处理异常内容
            for (Integer index : result.abnormalIndices) {
                processAbnormalSegment(result, index, apiKeys.get(random.nextInt(apiKeys.size())), apiUrl, model, chapter, aiPrompt, httpClient);
            }
        }finally {
            // 更新最终状态
            synchronized (chapter) {
                chapter.setTranslatorContent(String.join("\n", result.parts));
                chapter.setNowState(chapter.getNowState() == 3 ? 2 : 1);
                chapterExecuteRepository.save(chapter);
            }
        }


        return chapter;
    }

    public static Novelpia.ValidationResult panduanyichang(String content) {
        List<String> parts = new ArrayList<>();
        List<Integer> abnormalIndices = new ArrayList<>();
        final String START_TAG = "下方翻译出现异常！";
        final String END_TAG = "上方翻译出现异常！";
        final int TAG_LEN = START_TAG.length(); // 两个标记长度相同

        int startIndex = 0;
        int partIndex = 0;

        while (startIndex < content.length()) {
            int tagStart = content.indexOf(START_TAG, startIndex);
            int tagEnd = content.indexOf(END_TAG, tagStart == -1 ? startIndex : tagStart);

            // 如果没有找到完整标记对
            if (tagStart == -1 || tagEnd == -1) {
                String remaining = content.substring(startIndex).trim();
                if (!remaining.isEmpty() || parts.isEmpty()) {  // 保留最后一个空内容
                    parts.add(remaining);
                }
                break;
            }

            // 处理标记前的内容
            if (tagStart > startIndex) {
                String normal = content.substring(startIndex, tagStart).trim();
                if (!normal.isEmpty() || parts.isEmpty()) {
                    parts.add(normal);
                    partIndex++;
                }
            }

            // 处理异常内容
            String abnormal = content.substring(tagStart + TAG_LEN, tagEnd).trim();
            parts.add(abnormal);
            abnormalIndices.add(partIndex);
            partIndex++;

            // 移动起始位置
            startIndex = tagEnd + TAG_LEN;
        }

        return new Novelpia.ValidationResult(parts, abnormalIndices);
    }

    private void processAbnormalSegment(Novelpia.ValidationResult result, int index,
                                        PlatformApiKey apiKey, String apiUrl,
                                        String model, ChapterExecute chapter, String aiPrompt, OkHttpClient httpClient) {
        String original = result.parts.get(index);
        StringBuilder content = new StringBuilder();

        try {
            String translation = translation(apiKey.getApiKey(), apiUrl, original, model, true, aiPrompt, httpClient);
            content.append(translation);
        } catch (Exception e) {
            buildAbnormalContent(content, original);
            chapter.setNowState(1);
            e.printStackTrace();
        }

        result.parts.set(index, content.toString());
    }

    private void buildAbnormalContent(StringBuilder builder, String original) {
        builder.append("\n下方翻译出现异常！\n")
                .append(original)
                .append("\n上方翻译出现异常！\n");
    }


    private static CloseableHttpClient createHttpClient() {
        // 设置连接池参数
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(10); // 总的最大连接数
        connectionManager.setDefaultMaxPerRoute(5); // 每个路由的最大连接数

        // 设置超时时间（单位：毫秒）
        int connectionTimeout = 30000; // 连接超时时间：30秒
        int socketTimeout = 30000; // 请求超时时间：30秒
        int connectionRequestTimeout = 30000; // 连接请求超时时间：30秒

        // 创建请求配置
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setSocketTimeout(socketTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .build();

        // 创建 HttpClient
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultCookieStore(new BasicCookieStore()) // 设置默认的Cookie存储
                .setDefaultRequestConfig(requestConfig) // 设置默认的请求配置
                .build();
    }

    public static List<Integer> findMissingNumbers(Set<Integer> numbersSet, int num) {
        List<Integer> missingNumbers = new ArrayList<>();
        for (int i = 0; i <= num; i++) {
            if (!numbersSet.contains(i)) {
                missingNumbers.add(i);
            }
        }
        return missingNumbers;
    }

    public List<ChapterExecute> executeDownload() {
        List<Novel> novels = getNovels();
        novels.removeIf(novel -> executeId.contains(novel.getTrueId()));
        List<ChapterExecute> chapterExecuteList = Collections.synchronizedList(new ArrayList<>());
        int maxRetries = 3; // 最大重试次数
        int timeout = 30000; // 超时时间（30秒）
        final String epFormat = "EP%s";
        // 创建线程池（根据实际情况调整核心线程数）
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService executor = Executors.newFixedThreadPool(corePoolSize);
        String syosetuGetAllChapter = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("syosetuGetAllChapter").getValueField();
        try {
            List<CompletableFuture<Void>> futures = novels.stream()
                    .map(novel -> CompletableFuture.runAsync(() -> {
                        try {
                            // 获取当前小说的章节信息
                            List<Integer> existingChapters = chapterRepository
                                    .findChapterNumbersByNovelIdAndIsDeletedFalse(novel.getId());
                            List<Integer> executingChapters = chapterExecuteRepository
                                    .findChapterNumbersByNovelIdAndIsDeletedFalse(novel.getId());
                            Set<Integer> existingChaptersSet = new HashSet<>(existingChapters);
                            existingChaptersSet.addAll(executingChapters);

                            SyosetuNovelDetail syosetuNovelDetail = saveNovelDetail(novel.getTrueId());
                            if (syosetuNovelDetail.getNovelType().equals("短編")) {
                                if (existingChaptersSet.size() < 2) {
                                    if (!existingChaptersSet.contains(0)) {
                                        String episodeFile = String.format(epFormat, String.format("%04d", 0));
                                        ChapterExecute chapter = new ChapterExecute(
                                                novel.getId(),
                                                episodeFile,
                                                0,
                                                syosetuNovelDetail.getPrologue(),
                                                0,
                                                "0"
                                                ,false
                                        );
                                        chapterExecuteRepository.save(chapter);
                                    }
                                    if (!existingChaptersSet.contains(1)) {
                                        String shortNovel = getShortNovel(novel.getTrueId());
                                        String chapterNum = String.format(epFormat, String.format("%04d", 1));
                                        ChapterExecute chapterExecute = new ChapterExecute(
                                                novel.getId(),
                                                chapterNum,
                                                1,
                                                shortNovel,
                                                0,
                                                "1",
                                                false
                                        );
                                        chapterExecuteRepository.save(chapterExecute);
                                    }
                                }
                            } else {
                                String oneUrl = String.format(syosetuGetAllChapter, novel.getTrueId(), 1);
                                // 获取总章节数
                                int totalChapters = getTotalChapters(oneUrl, timeout, maxRetries);
                                if (totalChapters <= 0) {
                                    return;
                                }
                                List<Integer> missingNumbers = findMissingNumbers(existingChaptersSet, totalChapters);
                                List<ChapterExecute> chapterExecutes = new ArrayList<>();
                                // 这里的0章节，要单独处理
                                if (missingNumbers.contains(0)) {

                                    String episodeFile = String.format(epFormat, String.format("%04d", 0));
                                    ChapterExecute chapter = new ChapterExecute(
                                            novel.getId(),
                                            episodeFile,
                                            0,
                                            syosetuNovelDetail.getPrologue(),
                                            0,
                                            "0",
                                            false
                                    );
                                    chapterExecuteRepository.save(chapter);

                                    missingNumbers.remove(0);
                                }
                                Map<Integer, String> allChapterByChapterList = getAllChapterByChapterList(novel.getTrueId(), missingNumbers);
                                allChapterByChapterList.forEach((k, v) -> {
                                    String chapterNum = String.format(epFormat, String.format("%04d", k));
                                    ChapterExecute chapterExecute = new ChapterExecute(
                                            novel.getId(),
                                            chapterNum,
                                            k,
                                            v,
                                            0,
                                            String.valueOf(k),
                                            false
                                    );
                                    chapterExecutes.add(chapterExecute);
                                    chapterExecuteList.add(chapterExecute);
                                });
                                chapterExecuteRepository.saveAll(chapterExecutes);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, executor))
                    .toList();
            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        return chapterExecuteList;
    }

    // 获取未完结小说
    public List<Novel> getNovels() {
        String up = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("upNumber").getValueField();
        return novelRepository.findByPlatformEqualsAndUpGreaterThanAndIsDeletedFalseOrderByUpDesc("syosetu", Integer.parseInt(up));
    }

    public static List<Novel> mergeNovels(List<Novel> novelPia, List<Novel> novelPia1) {
        // 合并两个列表并去重
        return new ArrayList<>(Stream.concat(novelPia.stream(), novelPia1.stream())
                .collect(Collectors.toMap(
                        Novel::getId, // 使用id作为键
                        novel -> novel, // 值为novel对象
                        (existing, replacement) -> existing // 如果有重复的id，保留第一个
                )).values());
    }


//    ===========================================
//    ===========================================
//    ===========================================

    // 定时任务
//    @Scheduled(cron = "0 50 0/2 * * ?")
    public void fixErrorChapter() {
        if (executeError.compareAndSet(true, false)) {
            try {
                final boolean executeNovelPiaDownloadError = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeSyosetuDownloadError").getValueField());
                final boolean executeNovelPiaTrError = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeSyosetuTrError").getValueField());
                if (!executeNovelPiaDownloadError) {
                    return;
                }
                List<UserFeedback> byIsDeleteFalse = userFeedbackRepository.findByIsDeleteFalse();
                // 提取 novel_id 列表
                List<Long> novelIds = byIsDeleteFalse.stream()
                        .map(UserFeedback::getNovelId)
                        .toList();
                List<Novel> novelPia = novelRepository.findByIdInAndPlatformEqualsAndIsDeletedFalse(novelIds, "syosetu");
                // 提取 novelPia 中的 id 列表
                Set<Long> novelPiaIds = novelPia.stream()
                        .map(Novel::getId)
                        .collect(Collectors.toSet());

                // 筛选 byIsDeleteFalse 中 novelId 存在于 novelPiaIds 的数据
                List<UserFeedback> result = byIsDeleteFalse.stream()
                        .filter(userFeedback -> novelPiaIds.contains(userFeedback.getNovelId()))
                        .toList();

                List<Long> chaptersId = result.stream()
                        .map(UserFeedback::getChapterId)
                        .toList();
                List<Chapter> chapterList = chapterRepository.findByIdInAndIsDeletedFalse(chaptersId);
                // 最新下载的
                List<ChapterErrorExecute> chapterErrorExecutes = executeDownloadError(chapterList);
                if (!executeNovelPiaTrError) {
                    return;
                }
                List<ChapterErrorExecute> chapterErrorExecutesExceptions = executeTranslationError("siliconflow", chapterErrorExecutes);
                executeTranslationExceptionError("siliconflow", chapterErrorExecutesExceptions);
            } finally {
                executeError.set(true);
            }
        }
    }


    public List<ChapterErrorExecute> executeTranslationError(String platformName, List<ChapterErrorExecute> chapterErrorExecuteList) {
        // 初始化配置信息（保持单线程获取）
        String siliconflowApiUrl = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflow").getValueField();
        String siliconflowModel = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflowModel_1").getValueField();
        String aiPrompt = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("aiPrompt").getValueField();
        String siliconflowMaxLength = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflowMaxLength").getValueField();
        Platform platform = platformRepository.findPlatformByPlatformName(platformName);
        List<PlatformApiKey> apiKeys = platformApiKeyRepository.findByPlatformIdAndIsDeletedFalse(platform.getId());
        // 使用线程安全集合保存结果
        List<ChapterErrorExecute> chapterExecuteOverList = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        OkHttpClient httpClient = createOKHttpClient();
        try {
            // 并行处理每个章节
            List<CompletableFuture<Void>> futures = chapterErrorExecuteList.stream()
                    .filter(chapter -> chapter.getNowState() != 3)
                    .map(chapter -> CompletableFuture.runAsync(() ->
                                    processChapterError(chapter, apiKeys, siliconflowApiUrl,
                                            siliconflowModel, siliconflowMaxLength, chapterExecuteOverList,aiPrompt, httpClient),
                            executor))
                    .toList();

            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 销毁 OkHttpClient 实例
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
            executor.shutdown();
        }

        return chapterExecuteOverList;
    }

    private void processChapterError(ChapterErrorExecute chapterExecute, List<PlatformApiKey> apiKeys,
                                     String apiUrl, String model, String maxLengthStr,
                                     List<ChapterErrorExecute> resultList, String aiPrompt,OkHttpClient httpClient) {
        // 每个线程使用独立的随机数生成器
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int maxLength = Integer.parseInt(maxLengthStr);
        StringBuilder okContent = new StringBuilder();
        boolean hasError = false;

        try {
            chapterExecute.setContent(RemoveRepeatUtil.processString(chapterExecute.getContent()));
            // 状态预检查
            if (chapterExecute.getNowState() == 3) return;

            // 选择API Key（线程安全访问）
            PlatformApiKey apiKey = apiKeys.get(random.nextInt(apiKeys.size()));

            // 分割文本
            List<String> textParts = splitTextByLine(chapterExecute.getContent(), maxLength);

            // 更新状态需要加锁或使用乐观锁
            synchronized (chapterExecute) {
                chapterExecute.setNowState(3);
                chapterErrorExecuteRepository.save(chapterExecute);
            }

            // 处理每个文本片段
            for (String part : textParts) {
                part = processMultilineString(part);
                try {
//                    String translation = translation(apiKey.getApiKey(), apiUrl, part, model, true, aiPrompt, httpClient);
                    String translation = "dadas2312\n";
                    okContent.append(translation);
                } catch (Exception e) {
                    handleAbnormalTranslation(okContent, part);
                    hasError = true;
                    e.printStackTrace();
                }
            }

            // 更新最终状态
            synchronized (chapterExecute) {
                chapterExecute.setTranslatorContent(okContent.toString());
                chapterExecute.setNowState(hasError ? 1 : 2);

                if (hasError) {
                    resultList.add(chapterExecute);
                } else {
                    Chapter chapter = chapterRepository.findByNovelIdAndChapterNumberAndIsDeletedFalse(chapterExecute.getNovelId(), chapterExecute.getChapterNumber());
                    if (chapter != null) {
                        int length = chapter.getContent().length();
                        chapter.setChapterNumber(chapterExecute.getChapterNumber());
                        chapter.setContent(chapterExecute.getTranslatorContent());
                        chapter.setTitle(chapterExecute.getTitle());
                        chapter.setNovelId(chapterExecute.getNovelId());
                        chapter.setTrueId(chapterExecute.getTrueId());
                        chapter.setOwnPhoto(chapterExecute.isOwnPhoto());
                        chapterRepository.save(chapter);
                        novelRepository.incrementFontNumberById(chapter.getNovelId(), (long) (chapter.getContent().length() - length));
                        userFeedbackRepository.softDeleteByUserAndContent(chapterExecute.getNovelId(), chapter.getId());
                        chapterExecute.setDeleted(true);
                        chapterErrorExecuteRepository.softDeleteById(chapterExecute.getId());
                    } else {
                        Chapter chapter1 = new Chapter();
                        chapter1.setChapterNumber(chapterExecute.getChapterNumber());
                        chapter1.setContent(chapterExecute.getTranslatorContent());
                        chapter1.setTitle(chapterExecute.getTitle());
                        chapter1.setNovelId(chapterExecute.getNovelId());
                        chapter1.setTrueId(chapterExecute.getTrueId());
                        chapter1.setOwnPhoto(chapterExecute.isOwnPhoto());
                        chapterRepository.save(chapter1);
                        novelRepository.incrementFontNumberById(chapter1.getNovelId(), (long) chapter1.getContent().length());
                    }
                }

            }
        } catch (Exception e) {
            // 处理全局异常
            synchronized (chapterExecute) {
                chapterExecute.setNowState(1);
                resultList.add(chapterExecute);
            }
            e.printStackTrace();
        }finally {
            chapterErrorExecuteRepository.save(chapterExecute);
        }
    }

    public void executeTranslationExceptionError(String platformName, List<ChapterErrorExecute> chapterExecuteList) {
        // 预加载公共配置（线程安全方式）
        String apiUrl = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflow").getValueField();
        String model = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflowModel_1").getValueField();
        String aiPrompt = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("aiPrompt").getValueField();
        Platform platform = platformRepository.findPlatformByPlatformName(platformName);
        List<PlatformApiKey> apiKeys = platformApiKeyRepository.findByPlatformIdAndIsDeletedFalse(platform.getId());
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        OkHttpClient httpClient = createOKHttpClient();
        try {
            List<CompletableFuture<Void>> futures = chapterExecuteList.stream()
                    .filter(chapter -> chapter.getNowState() != 3)
                    .map(chapter -> CompletableFuture.runAsync(() -> {
                        try {
                            // 处理单个章节
                            ChapterErrorExecute executed = processSingleChapterError(chapter, platformName, apiUrl, model, apiKeys, aiPrompt, httpClient);

                            // 保存最终章节
                            if (executed.getNowState() == 2) {
                                saveFinalChapterError(executed);
                            }
                        } catch (Exception e) {
                            handleChapterError(chapter, e);
                        }
                    }, executor))
                    .toList();
            // 等待所有任务完成
            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Processing interrupted", e);
            }
        } finally {
            executor.shutdown();
            // 销毁 OkHttpClient 实例
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }

    }

    private synchronized void handleChapterError(ChapterErrorExecute chapter, Exception e) {
        chapter.setNowState(1);
        chapterErrorExecuteRepository.save(chapter);
        e.printStackTrace();
    }

    private synchronized void saveFinalChapterError(ChapterErrorExecute executed) {
        Chapter chapter = chapterRepository.findByNovelIdAndChapterNumberAndIsDeletedFalse(executed.getNovelId(), executed.getChapterNumber());
        if (chapter != null) {
            int length = chapter.getContent().length();
            chapter.setContent(executed.getTranslatorContent());
            chapterRepository.save(chapter);
            novelRepository.incrementFontNumberById(chapter.getNovelId(), (long) (chapter.getContent().length() - length));
            userFeedbackRepository.softDeleteByUserAndContent(executed.getNovelId(), chapter.getId());
            chapterErrorExecuteRepository.softDeleteById(executed.getId());
        } else {
            // 只要程序没问题，不可能进入这个逻辑
            Chapter chapter1 = new Chapter();
            chapter1.setChapterNumber(executed.getChapterNumber());
            chapter1.setContent(executed.getTranslatorContent());
            chapter1.setTitle(executed.getTitle());
            chapter1.setNovelId(executed.getNovelId());
            chapter1.setOwnPhoto(executed.isOwnPhoto());
            chapter1.setTrueId(executed.getTrueId());
            chapterRepository.save(chapter1);
            novelRepository.incrementFontNumberById(chapter1.getNovelId(), (long) chapter1.getContent().length());
        }
    }

    private ChapterErrorExecute processSingleChapterError(ChapterErrorExecute chapter, String platformName,
                                                          String apiUrl, String model, List<PlatformApiKey> apiKeys, String aiPrompt, OkHttpClient httpClient) {
        // 使用线程安全的随机数
        ThreadLocalRandom random = ThreadLocalRandom.current();
        chapter.setTranslatorContent(chapter.getTranslatorContent().replaceAll("V1Zn[A-Za-z0-9+=]+", ""));
        // 状态检查和初始化
        if (chapter.getNowState() == 3) return chapter;
        Novelpia.ValidationResult result = panduanyichang(chapter.getTranslatorContent());
        try {
            // 更新状态需要同步
            synchronized (chapter) {
                chapter.setNowState(3);
                chapterErrorExecuteRepository.save(chapter);
            }

            // 处理异常内容
            for (Integer index : result.abnormalIndices) {
                processAbnormalSegmentError(result, index, apiKeys.get(random.nextInt(apiKeys.size())), apiUrl, model, chapter, aiPrompt, httpClient);
            }
        }finally {
            // 更新最终状态
            synchronized (chapter) {
                chapter.setTranslatorContent(String.join("\n", result.parts));
                chapter.setNowState(chapter.getNowState() == 3 ? 2 : 1);
                chapterErrorExecuteRepository.save(chapter);
            }
        }


        return chapter;
    }

    private void processAbnormalSegmentError(Novelpia.ValidationResult result, int index,
                                             PlatformApiKey apiKey, String apiUrl,
                                             String model, ChapterErrorExecute chapter, String aiPrompt, OkHttpClient httpClient) {
        String original = result.parts.get(index);
        StringBuilder content = new StringBuilder();

        try {
            String translation = translation(apiKey.getApiKey(), apiUrl, original, model, true, aiPrompt, httpClient);
            content.append(translation);
        } catch (Exception e) {
            buildAbnormalContent(content, original);
            chapter.setNowState(1);
            e.printStackTrace();
        }

        result.parts.set(index, content.toString());
    }


    public List<ChapterErrorExecute> executeDownloadError(List<Chapter> chapterList) {
        List<ChapterErrorExecute> chapterExecuteList = Collections.synchronizedList(new ArrayList<>());
        int maxRetries = 3; // 最大重试次数
        int timeout = 30000; // 超时时间（30秒）
        final String epFormat = "EP%s";
        // 创建线程池（根据实际情况调整核心线程数）
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService executor = Executors.newFixedThreadPool(corePoolSize);
        try {
            List<CompletableFuture<Void>> futures = chapterList.stream()
                    .map(chapter -> CompletableFuture.runAsync(() -> {
                        try {
                            ChapterErrorExecute byNovelIdAndChapterNumberAndIsDeletedFalse = chapterErrorExecuteRepository.findByNovelIdAndChapterNumber(chapter.getNovelId(), chapter.getChapterNumber());
                            if (byNovelIdAndChapterNumberAndIsDeletedFalse != null && !byNovelIdAndChapterNumberAndIsDeletedFalse.isDeleted()) {
                                chapterExecuteList.add(byNovelIdAndChapterNumberAndIsDeletedFalse);
                                return;
                            }
                            Novel novel = novelRepository.findByIdAndIsDeletedFalse(chapter.getNovelId());
                            SyosetuNovelDetail syosetuNovelDetail = saveNovelDetail(novel.getTrueId());
                            if (syosetuNovelDetail.getNovelType().equals("短編")) {
                                if (chapter.getTrueId().equals("0")) {
                                    if (byNovelIdAndChapterNumberAndIsDeletedFalse != null && byNovelIdAndChapterNumberAndIsDeletedFalse.isDeleted()){
                                        byNovelIdAndChapterNumberAndIsDeletedFalse.setDeleted(false);
                                        byNovelIdAndChapterNumberAndIsDeletedFalse.setContent(syosetuNovelDetail.getPrologue());
                                        byNovelIdAndChapterNumberAndIsDeletedFalse.setNowState(0);
                                        byNovelIdAndChapterNumberAndIsDeletedFalse.setTranslatorContent("");
                                        ChapterErrorExecute save = chapterErrorExecuteRepository.save(byNovelIdAndChapterNumberAndIsDeletedFalse);
                                        chapterExecuteList.add(save);
                                        return;
                                    }
                                    ChapterErrorExecute chapterErrorExecute = new ChapterErrorExecute(
                                            novel.getId(),
                                            chapter.getTitle(),
                                            0,
                                            syosetuNovelDetail.getPrologue(),
                                            0,
                                            "0"
                                            ,false
                                    );
                                    ChapterErrorExecute save = chapterErrorExecuteRepository.save(chapterErrorExecute);
                                    chapterExecuteList.add(save);
                                }
                                if (chapter.getTrueId().equals("1")) {
                                    String shortNovel = getShortNovel(novel.getTrueId());
                                    if (byNovelIdAndChapterNumberAndIsDeletedFalse != null && byNovelIdAndChapterNumberAndIsDeletedFalse.isDeleted()){
                                        byNovelIdAndChapterNumberAndIsDeletedFalse.setDeleted(false);
                                        byNovelIdAndChapterNumberAndIsDeletedFalse.setContent(shortNovel);
                                        byNovelIdAndChapterNumberAndIsDeletedFalse.setNowState(0);
                                        ChapterErrorExecute save = chapterErrorExecuteRepository.save(byNovelIdAndChapterNumberAndIsDeletedFalse);
                                        chapterExecuteList.add(save);
                                        return;
                                    }
                                    ChapterErrorExecute chapterErrorExecute = new ChapterErrorExecute(
                                            novel.getId(),
                                            chapter.getTitle(),
                                            1,
                                            shortNovel,
                                            0,
                                            "1",
                                            false
                                    );
                                    ChapterErrorExecute save = chapterErrorExecuteRepository.save(chapterErrorExecute);
                                    chapterExecuteList.add(save);
                                }
                            } else {
                                // 这里的0章节，要单独处理
                                if (chapter.getTrueId().equals("0")) {
                                    if (byNovelIdAndChapterNumberAndIsDeletedFalse != null && byNovelIdAndChapterNumberAndIsDeletedFalse.isDeleted()){
                                        byNovelIdAndChapterNumberAndIsDeletedFalse.setDeleted(false);
                                        byNovelIdAndChapterNumberAndIsDeletedFalse.setContent(syosetuNovelDetail.getPrologue());
                                        byNovelIdAndChapterNumberAndIsDeletedFalse.setNowState(0);
                                        ChapterErrorExecute save = chapterErrorExecuteRepository.save(byNovelIdAndChapterNumberAndIsDeletedFalse);
                                        chapterExecuteList.add(save);
                                        return;
                                    }
                                    ChapterErrorExecute chapterErrorExecute = new ChapterErrorExecute(
                                            novel.getId(),
                                            chapter.getTitle(),
                                            0,
                                            syosetuNovelDetail.getPrologue(),
                                            0,
                                            "0",
                                            false
                                    );
                                    ChapterErrorExecute save = chapterErrorExecuteRepository.save(chapterErrorExecute);
                                    chapterExecuteList.add(save);
                                } else {
                                    String novelContent = getOneChapterByChapterList(novel.getTrueId(), chapter.getTrueId());
                                    if (byNovelIdAndChapterNumberAndIsDeletedFalse != null && byNovelIdAndChapterNumberAndIsDeletedFalse.isDeleted()){
                                        byNovelIdAndChapterNumberAndIsDeletedFalse.setDeleted(false);
                                        byNovelIdAndChapterNumberAndIsDeletedFalse.setContent(novelContent);
                                        byNovelIdAndChapterNumberAndIsDeletedFalse.setNowState(0);
                                        ChapterErrorExecute save = chapterErrorExecuteRepository.save(byNovelIdAndChapterNumberAndIsDeletedFalse);
                                        chapterExecuteList.add(save);
                                        return;
                                    }
                                    ChapterErrorExecute chapterErrorExecute = new ChapterErrorExecute(
                                            novel.getId(),
                                            chapter.getTitle(),
                                            chapter.getChapterNumber(),
                                            novelContent,
                                            0,
                                            chapter.getTrueId(),
                                            false
                                    );
                                    ChapterErrorExecute save = chapterErrorExecuteRepository.save(chapterErrorExecute);
                                    chapterExecuteList.add(save);
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, executor))
                    .toList();
            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        return chapterExecuteList;
    }

    public String getOneChapterByChapterList(String novelTrueId, String chapterId) {
        String syosetuGetAllChapter = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("syosetuGetAllChapter").getValueField();
        String novelContent = "";
        int maxRetries = 3; // 最大重试次数
        int timeout = 30000; // 超时时间（30秒）
        try {
            String chapterUrl = String.format(syosetuGetAllChapter ,novelTrueId, chapterId);
            StringBuilder chapterContent = new StringBuilder();

            // 重试机制
            for (int retry = 0; retry < maxRetries; retry++) {
                try {
                    Document chapterDoc = Jsoup.connect(chapterUrl)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .timeout(timeout)
//                            .proxy(proxy)
                            .get();

                    // 获取标题
                    Elements titleElements = chapterDoc.getElementsByClass("p-novel__title");
                    for (Element element : titleElements) {
                        chapterContent.append(element.text()).append("\n");
                    }

                    // 获取正文内容
                    Elements bodyElements = chapterDoc.getElementsByClass("p-novel__body");
                    for (Element element : bodyElements) {
                        String htmlContent = element.html();
                        String textWithNewlines = htmlContent.replaceAll("<br\\s*/?>", "\n");
                        textWithNewlines = textWithNewlines.replaceAll("<[^>]+>", "");
                        String[] lines = textWithNewlines.split("\n");
                        for (String line : lines) {
                            String trimmedLine = line.trim();
                            if (!trimmedLine.isEmpty()) {
                                chapterContent.append(trimmedLine).append("\n");
                            }
                        }
                    }
                    novelContent = chapterContent.toString();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return novelContent;
    }

}