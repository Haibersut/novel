package com.wtl.novel.translator;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.wtl.novel.CDO.*;
import com.wtl.novel.DTO.UserIdProjection;
import com.wtl.novel.Service.*;
import com.wtl.novel.entity.*;
import com.wtl.novel.repository.*;
import com.wtl.novel.util.*;
import okhttp3.*;
import com.fasterxml.jackson.core.JsonParseException;
import okio.BufferedSource;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;



@Component
public class Novelpia {

    @Autowired
    private UserFeedbackRepository userFeedbackRepository;
    @Autowired
    private ChapterSyncService chapterSyncService;
    @Autowired
    private DictionaryRepository dictionaryRepository;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private TerminologyService terminologyService;
    @Autowired
    private NovelRepository novelRepository;
    @Autowired
    private NovelService novelService;

    @Autowired
    private NovelTagRepository novelTagRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private MessageService messageService;

    @Autowired
    private PlatformApiKeyRepository platformApiKeyRepository;
    @Autowired
    private PlatformRepository platformRepository;
    @Autowired
    private ChapterImageLinkRepository chapterImageLinkRepository;

    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private ChapterExecuteRepository chapterExecuteRepository;
    @Autowired
    private CloudflareR2Util cloudflareR2Util;

    private static final AtomicBoolean downloaded = new AtomicBoolean(true);
    private static final AtomicBoolean executePhoto = new AtomicBoolean(true);
    private static final AtomicBoolean translation = new AtomicBoolean(true);
    private static final AtomicBoolean executeError = new AtomicBoolean(true);
    private static final AtomicBoolean executeUploadTranslationTag = new AtomicBoolean(true);
    private static final AtomicBoolean executeUploadTranslationExceptionTag = new AtomicBoolean(true);


    private static final String imgIndex = "%s_%s"; // 替换为你的代理IP

    @Value("${proxy.host}")
    private String proxyHost;

    @Value("${proxy.port}")
    private int proxyPort;

    private static final int poolSize = Runtime.getRuntime().availableProcessors() * 60;

    private static final String regex = "<img\\s+[^>]*>";
    private static final String regex1 =
            "<p\\s+style=['\"]\\s*height\\s*:\\s*0px\\s*;\\s*width\\s*:\\s*0px\\s*;\\s*overflow\\s*:\\s*hidden\\s*;\\s*opacity\\s*:\\s*0\\s*;\\s*display\\s*:\\s*inline-block\\s*;?['\"]\\s*>" +
                    "([^<]*?)" +
                    "</p>";
    // 创建代理对象
    Set<String> executeId = ConcurrentHashMap.newKeySet();
    @Autowired
    private ChapterErrorExecuteRepository chapterErrorExecuteRepository;

    private static Set<Long> downArraySetWait = new CopyOnWriteArraySet<>();
    private static Set<Long> downArraySetRun = new CopyOnWriteArraySet<>();
    private static Set<Long> copyOnWriteArraySetWait = new CopyOnWriteArraySet<>();
    private static Set<Long> copyOnWriteArraySetExceptionWait = new CopyOnWriteArraySet<>();
    private static Set<Long> copyOnWriteArraySetRun = new CopyOnWriteArraySet<>();
    private static Set<Long> copyOnWriteArraySetExceptionRun = new CopyOnWriteArraySet<>();

    private static Set<Long> uploadCopyOnWriteArraySetRun = new CopyOnWriteArraySet<>();
    private static Set<Long> uploadCopyOnWriteArraySetWait = new CopyOnWriteArraySet<>();
    private static Set<Long> uploadCopyOnWriteArraySetExceptionRun = new CopyOnWriteArraySet<>();
    private static Set<Long> uploadCopyOnWriteArraySetExceptionWait = new CopyOnWriteArraySet<>();


    public void executeTask1() {
        final boolean executeDownload = true;
        if (executeDownload) {
            System.out.println("开始准备下载");
            System.out.println("开始准备下载");
            System.out.println("开始准备下载");
            List<ChapterExecute> chapterExecutes = executeDownload();
            System.out.println("定时任务执行: " + LocalDateTime.now());
        }
    }


    public void executeTask1_1() {
        final boolean executeDownload = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeDownload").getValueField());
        if (executeDownload) {
            System.out.println("开始准备下载");
            System.out.println("开始准备下载");
            System.out.println("开始准备下载");
            List<ChapterExecute> chapterExecutes = executeDownload();
            List<Novel> novels = getNovels();
            List<Long> novelIdList = novels.stream().map(Novel::getId).toList();
            List<ChapterExecute> executeList = chapterExecuteRepository.findByNovelIdsAndNowStateAndIsDeletedFalse(novelIdList, 0);
            chapterExecutes.addAll(executeList);
            Set<Long> novelIds = chapterExecutes.stream()
                    .map(ChapterExecute::getNovelId)
                    .collect(Collectors.toSet());
            novelIds.stream()
                    .filter(id -> !copyOnWriteArraySetRun.contains(id))
                    .forEach(copyOnWriteArraySetWait::add);


            List<ChapterExecute> executeExectionList = chapterExecuteRepository.findByNovelIdsAndNowStateAndIsDeletedFalse(novelIdList, 1);
            Set<Long> exceptionNovelIds = executeExectionList.stream()
                    .map(ChapterExecute::getNovelId)
                    .collect(Collectors.toSet());
            exceptionNovelIds.stream()
                    .filter(id -> !copyOnWriteArraySetExceptionRun.contains(id))
                    .forEach(copyOnWriteArraySetExceptionWait::add);
            System.out.println("定时任务执行: " + LocalDateTime.now());
        }
    }

    public void executeTask2() {
        final boolean executeTr = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeTr").getValueField());
        if (executeTr) {
            List<Novel> novels = getNovels();
            List<Long> novelIdList = novels.stream() .filter(novel -> !Objects.equals(novel.getNovelType(), "删除")).map(Novel::getId).toList();
            Set<Long> executeList = chapterExecuteRepository.findDistinctNovelIdsByNovelIdsAndNowStateAndIsDeletedFalse(novelIdList, 0);
            executeList.stream()
                    .filter(id -> !copyOnWriteArraySetRun.contains(id))
                    .forEach(copyOnWriteArraySetWait::add);


            repeat2();
        }
    }
    public void repeat2() {
        boolean executeTr = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeTr").getValueField());
        System.out.println(executeTr && !copyOnWriteArraySetWait.isEmpty() && copyOnWriteArraySetRun.size() < 15);
        if (executeTr && !copyOnWriteArraySetWait.isEmpty() && copyOnWriteArraySetRun.size() < 15) {
            System.out.println("开始执行repeat2");
            List<Long> elementsToRemove = copyOnWriteArraySetWait.stream()
                    .filter(element -> !copyOnWriteArraySetRun.contains(element))
                    .limit(15 - copyOnWriteArraySetRun.size() - copyOnWriteArraySetExceptionRun.size())
                    .toList();
            copyOnWriteArraySetRun.addAll(elementsToRemove);
            elementsToRemove.forEach(copyOnWriteArraySetWait::remove);
            List<ChapterExecute> chapters = chapterExecuteRepository.findByNovelIdsAndNowStateAndIsDeletedFalse(elementsToRemove,0);
            String platformName = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("enableTrModel").getValueField();
            asyncExecuteTranslation(platformName, chapters, elementsToRemove.size());
        }
    }

    public void executeTask3() {
        final boolean executeTr = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeTr").getValueField());
        if (executeTr) {
            List<Novel> novels = getNovels();
            List<Long> novelIdList = novels.stream().filter(novel -> !Objects.equals(novel.getNovelType(), "删除")).map(Novel::getId).toList();
            Set<Long> executeExectionList = chapterExecuteRepository.findDistinctNovelIdsByNovelIdsAndNowStateAndIsDeletedFalse(novelIdList, 1);
            executeExectionList.stream()
                    .filter(id -> !copyOnWriteArraySetExceptionRun.contains(id))
                    .forEach(copyOnWriteArraySetExceptionWait::add);


            repeat3();
        }
    }


    public void repeat3() {
        boolean executeTr = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeTr").getValueField());
        System.out.println(executeTr && !copyOnWriteArraySetExceptionWait.isEmpty() && copyOnWriteArraySetExceptionRun.size() < 10);
        if (executeTr && !copyOnWriteArraySetExceptionWait.isEmpty() && copyOnWriteArraySetExceptionRun.size() < 10) {
            System.out.println("开始执行repeat3");
            System.out.println("开始执行repeat3");
            List<Long> elementsToRemove = copyOnWriteArraySetExceptionWait.stream()
                    .filter(element -> !copyOnWriteArraySetExceptionRun.contains(element))
                    .limit(10 - copyOnWriteArraySetRun.size() - copyOnWriteArraySetExceptionRun.size())
                    .toList();
            copyOnWriteArraySetExceptionRun.addAll(elementsToRemove);
            elementsToRemove.forEach(copyOnWriteArraySetExceptionWait::remove);
            List<ChapterExecute> chapters = chapterExecuteRepository.findByNovelIdsAndNowStateAndIsDeletedFalse(elementsToRemove,1);
            String platformName = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("enableTrModel").getValueField();
            asyncExecuteTranslationException(platformName, chapters, elementsToRemove.size());
        }
    }

    public void executeTask5() {
        System.out.println("copyOnWriteArraySetWait===>" + new Gson().toJson(copyOnWriteArraySetWait));
        System.out.println("copyOnWriteArraySetRun===>" + new Gson().toJson(copyOnWriteArraySetRun));
        System.out.println("copyOnWriteArraySetExceptionWait===>" + new Gson().toJson(copyOnWriteArraySetExceptionWait));
        System.out.println("copyOnWriteArraySetExceptionRun===>" + new Gson().toJson(copyOnWriteArraySetExceptionRun));
        System.out.println("uploadCopyOnWriteArraySetExceptionWait===>" + new Gson().toJson(uploadCopyOnWriteArraySetExceptionWait));
        System.out.println("uploadCopyOnWriteArraySetExceptionRun===>" + new Gson().toJson(uploadCopyOnWriteArraySetExceptionRun));
        System.out.println("uploadCopyOnWriteArraySetWait===>" + new Gson().toJson(uploadCopyOnWriteArraySetWait));
        System.out.println("uploadCopyOnWriteArraySetRun===>" + new Gson().toJson(uploadCopyOnWriteArraySetRun));
    }

    public void photo() {
        boolean executePhoto = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executePhoto").getValueField());
        if (executePhoto) {
            if (downloaded.compareAndSet(true, false)) {
                try {
                    List<Novel> novels = getNovels();
                    for (Novel novel : novels) {
                        if (Objects.equals(novel.getNovelType(), "删除")) {
                            continue;
                        }
                        System.out.println(novel.getId());
                        List<String> trueIds = chapterRepository.findTrueIdsByNovelIdAndIsDeletedFalse(novel.getId());
                        if (trueIds.isEmpty()) {
                            continue;
                        }
                        List<ChapterImageLink> allByChapterTrueIdIn = chapterImageLinkRepository.findAllByChapterTrueIdInAndCf(trueIds, false);
                        if (allByChapterTrueIdIn.isEmpty()) {
                            continue;
                        }
                        Map<ChapterImageLink, Boolean> existingObjects = new HashMap<>();
                        List<ChapterImageLink> uniqueChapterImageLinks = new ArrayList<>();
                        List<ChapterImageLink> duplicateChapterImageLinks = new ArrayList<>();
                        for (ChapterImageLink chapterImageLink : allByChapterTrueIdIn) {
                            if (!existingObjects.containsKey(chapterImageLink)) {
                                uniqueChapterImageLinks.add(chapterImageLink);
                                existingObjects.put(chapterImageLink, true);
                            } else {
                                duplicateChapterImageLinks.add(chapterImageLink);
                            }
                        }
                        chapterImageLinkRepository.deleteAll(duplicateChapterImageLinks);
                        for (ChapterImageLink image : uniqueChapterImageLinks) {
                            String contentLink = image.getContentLink();
                            Matcher matcher = Pattern.compile("src=\"([^\"]+)\"").matcher(contentLink);
                            if (matcher.find()) {
                                String imgSrc = matcher.group(1);
                                // 处理相对路径
                                if (imgSrc.startsWith("//")) {
                                    imgSrc = "https:" + imgSrc;
                                }

                                // 下载图片到本地
                                String localFilePath = cloudflareR2Util.getTempDirectory() + novel.getId() + File.separator + imgSrc.substring(imgSrc.lastIndexOf('/') + 1);
                                cloudflareR2Util.downloadImageToLocalStorage(imgSrc, localFilePath);
                                if (localFilePath != null) {
                                    // 上传到 Cloudflare R2
                                    String objectKey = imgSrc.substring(imgSrc.lastIndexOf('/') + 1);
                                    String newImageUrl = cloudflareR2Util.uploadImageToCloudflareR2(localFilePath, objectKey);

                                    if (newImageUrl != null) {
                                        // 将新的图片链接写入到 contentLink 的 <img> 标签中
                                        String updatedContentLink = cloudflareR2Util.replaceImageUrl(contentLink, imgSrc.replace("https:", ""), newImageUrl.replace("photo.1695ffc38d5faf9827f7fbd055757d56.r2.cloudflarestorage.com", "jpg.freenovel.sbs"));
                                        image.setContentLink(updatedContentLink);
                                        image.setCf(true);
                                        System.out.println("图片处理成功，ID: " + image.getId() + ", 新链接: " + newImageUrl);
                                    } else {
                                        System.err.println("上传图片到 Cloudflare R2 失败，ID: " + image.getId());
                                    }

                                    // 删除本地临时文件
                                    new File(localFilePath).delete();
                                } else {
                                    System.err.println("下载图片失败，ID: " + image.getId());
                                }
                            }
                        }
                        chapterImageLinkRepository.saveAll(uniqueChapterImageLinks);
                    }
                } finally {
                    downloaded.set(false);
                }
            }
        }
    }


    public void executeUploadTranslation() {
        final boolean executeUploadTranslation = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeUploadTranslation").getValueField());
        if (executeUploadTranslation) {
            List<Novel> novels = getUploadNovels();
            List<Long> novelIdList = novels.stream() .filter(novel -> !Objects.equals(novel.getNovelType(), "删除")).map(Novel::getId).toList();
            Set<Long> executeList = chapterExecuteRepository.findDistinctNovelIdsByNovelIdsAndNowStateAndIsDeletedFalse(novelIdList, 0);
            uploadCopyOnWriteArraySetWait.addAll(executeList);
            if (!uploadCopyOnWriteArraySetWait.isEmpty() && uploadCopyOnWriteArraySetRun.size() < 10) {
                System.out.println("开始执行用户上传executeUploadTranslation");
                System.out.println("开始执行用户上传executeUploadTranslation");
                List<Long> elementsToRemove = uploadCopyOnWriteArraySetWait.stream()
                        .filter(element -> !uploadCopyOnWriteArraySetRun.contains(element))
                        .limit(10 - uploadCopyOnWriteArraySetExceptionRun.size() - uploadCopyOnWriteArraySetRun.size())
                        .toList();
                uploadCopyOnWriteArraySetRun.addAll(elementsToRemove);
                elementsToRemove.forEach(uploadCopyOnWriteArraySetWait::remove);

                List<ChapterExecute> chapters = chapterExecuteRepository.findByNovelIdsAndNowStateAndIsDeletedFalse(elementsToRemove, 0);
                String platformName = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("enableTrModel").getValueField();
                asyncExecuteTranslation(platformName, chapters, elementsToRemove.size());
            }

        }
    }
//    uploadCopyOnWriteArraySetExceptionRun
    public void executeUploadTranslationException() {
        final boolean executeUploadTranslation = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeUploadTranslation").getValueField());
        if (executeUploadTranslation) {
            List<Novel> novels = getUploadNovels();
            List<Long> novelIdList = novels.stream() .filter(novel -> !Objects.equals(novel.getNovelType(), "删除")).map(Novel::getId).toList();
            Set<Long> executeList = chapterExecuteRepository.findDistinctNovelIdsByNovelIdsAndNowStateAndIsDeletedFalse(novelIdList, 1);
            uploadCopyOnWriteArraySetExceptionWait.addAll(executeList);

            if (!uploadCopyOnWriteArraySetExceptionWait.isEmpty() && uploadCopyOnWriteArraySetExceptionRun.size() < 10) {
                System.out.println("开始执行用户上传异常executeUploadTranslationException");
                System.out.println("开始执行用户上传异常executeUploadTranslationException");
                List<Long> elementsToRemove = uploadCopyOnWriteArraySetExceptionWait.stream()
                        .filter(element -> !uploadCopyOnWriteArraySetExceptionRun.contains(element))
                        .limit(10 - uploadCopyOnWriteArraySetExceptionRun.size() - uploadCopyOnWriteArraySetRun.size())
                        .toList();
                uploadCopyOnWriteArraySetExceptionRun.addAll(elementsToRemove);
                elementsToRemove.forEach(uploadCopyOnWriteArraySetExceptionWait::remove);
                String platformName = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("enableTrModel").getValueField();
                List<ChapterExecute> executeExectionList = chapterExecuteRepository.findByNovelIdsAndNowStateAndIsDeletedFalse(elementsToRemove, 1);
                asyncExecuteTranslationException(platformName, executeExectionList, elementsToRemove.size());
            }
        }
    }


    public void asyncExecuteTranslation(String arg1, List<ChapterExecute> chapters, int size) {
        executeTranslation(arg1, chapters, size);
    }


    public void asyncExecuteTranslationException(String arg1, List<ChapterExecute> chapters, int size) {
        executeTranslationException(arg1, chapters, size);
    }

    public List<NpPlatform> getNpPlatform() {
        String npPlatformJson = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("NpPlatform").getValueField();
        ObjectMapper mapper = new ObjectMapper();
        // 直接映射到 JsonData 对象
        try {
            return mapper.readValue(npPlatformJson, new TypeReference<List<NpPlatform>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @Cacheable(
            value = "npNovelCache",                      // 缓存名称
            key = "#novelActiveMap.platformName + '-' + #novelActiveMap.tag + '-' + #novelActiveMap.plus + '-' + #novelActiveMap.limit + '-' + #novelActiveMap.time + '-' + #novelActiveMap.sort + '-' + #novelActiveMap.update",
            unless = "#result == null"
    )
    public NpNovel getNpPlatformPage(NovelActiveMap novelActiveMap) {
        try {
            if (NovelpiaUrlBuilder.PLATFORMS == null) {
                NovelpiaUrlBuilder.PLATFORMS = getNpPlatform();
            }
            String buildUrl = NovelpiaUrlBuilder.build(novelActiveMap);
            final String cookie = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("theNovelPiaCookie").getValueField();
            if (cookie == null || cookie.isEmpty()) {
                throw new RuntimeException("当前无可用的cookie");
            }
            final boolean proxyClient = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("proxyClient").getValueField());
            CloseableHttpClient httpClient = proxyClient ? proxyHttpClient : nonProxyHttpClient;
            Map<String, String> formParams = new HashMap<>();
            formParams.put("cmd","weekly_ranking");
            String s = sendPostRequest(httpClient,buildUrl, cookie, formParams);
            if (novelActiveMap.getPlatformName().equals("top100")) {
                List<SimpleNovel> list = new ArrayList<>();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(s);
                JsonNode novels = root.path("list");

                for (JsonNode n : novels) {
                    SimpleNovel vo = new SimpleNovel();
                    vo.setNovelName(n.get("novel_name").asText());
                    vo.setNovelGenre(mapper.convertValue(n.get("novel_genre"), List.class));
                    vo.setCountView(n.get("count_view").asText());
                    vo.setCountBook(n.get("count_book").asInt());
                    vo.setWriterNick(n.get("writer_nick").asText());
                    vo.setTrueId(n.get("novel_no").asText());
                    list.add(vo);
                }

                // 2. 一次性拿到所有 trueId
                Set<String> trueIds = list.stream()
                        .map(SimpleNovel::getTrueId)
                        .collect(Collectors.toSet());




                List<Novel> allByTrueIdIn = novelRepository.findAllByTrueIdIn(trueIds);
                List<NovelCTO> novelCTOS = novelService.convertNpNovel(allByTrueIdIn);
                Map<String, NovelCTO> novelMap = novelCTOS
                        .stream()
                        .collect(Collectors.toMap(NovelCTO::getTrueId, v -> v));

                list.forEach(vo -> vo.setNovel(novelMap.get(vo.getTrueId())));



                return new NpNovel(list,null);
            }else {
                List<NovelBox> novelBoxes = extractTop100(s);

                // 2. 一次性拿到所有 trueId
                Set<String> trueIds = novelBoxes.stream()
                        .map(NovelBox::getTrueId)
                        .collect(Collectors.toSet());




                List<Novel> allByTrueIdIn = novelRepository.findAllByTrueIdIn(trueIds);
                List<NovelCTO> novelCTOS = novelService.convertNpNovel(allByTrueIdIn);
                Map<String, NovelCTO> novelMap = novelCTOS
                        .stream()
                        .collect(Collectors.toMap(NovelCTO::getTrueId, v -> v));

                novelBoxes.forEach(vo -> vo.setNovel(novelMap.get(vo.getTrueId())));

                return new NpNovel(null, novelBoxes);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<NovelBox> extractTop100(String html) {
        List<NovelBox> result = new ArrayList<>();

        if (isInvalidHtml(html)) return result;

        Document doc = Jsoup.parse(html);
        // 1. 先拿所有 novelbox
        Elements boxes = doc.select("#top100_page [class*=novelbox]");

        for (Element box : boxes) {
            NovelBox nb = new NovelBox();

            /* 1. 新增：取第一个子 div 的 onclick 并切出 trueId */
            Element firstDiv = box.child(0);          // novelbox 下第一个子 div
            if (firstDiv != null) {
                String onclick = firstDiv.attr("onclick");
                // 正则提取数字
                Matcher m = Pattern.compile("location='/novel/(\\d+)'").matcher(onclick);
                nb.setTrueId(m.find() ? m.group(1) : "");
            }

            /* 2. 原有逻辑继续 */
            nb.setFavCount(box.selectFirst(".thumb_s4") != null
                    ? box.selectFirst(".thumb_s4").text().trim() : "");

            nb.setChapCount(box.selectFirst(".thumb_s2") != null
                    ? box.selectFirst(".thumb_s2").text().trim() : "");

            Element title = box.selectFirst(".cut_line_one");
            nb.setBookName(title != null ? title.text().trim() : "");

            Element font = title == null ? null : title.nextElementSibling();
            if (font != null && "font".equals(font.tagName())) {
                Element authorFont = font.selectFirst("font");
                nb.setAuthor(authorFont != null ? authorFont.text().trim() : "");

                nb.setTags(font.nextElementSiblings().select("span").stream()
                        .map(Element::text)
                        .map(String::trim)
                        .collect(Collectors.toList()));
            }

            result.add(nb);
        }
        return result;
    }

    private String sendPostRequest(CloseableHttpClient httpClient,
                                   String url, String cookie,
                                   Map<String, String> formParams) throws Exception {

        HttpPost request = new HttpPost(url);
        if (cookie != null && !cookie.isEmpty()) {
            request.setHeader("Cookie", cookie);
        }
        List<NameValuePair> params = new ArrayList<>();
        Optional.ofNullable(formParams).orElse(Map.of())
                .forEach((k, v) -> params.add(new BasicNameValuePair(k, v)));
        request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        // 不要在这里 new httpClient，也不要关闭 httpClient
        CloseableHttpResponse response = httpClient.execute(request);
        try {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } finally {
            response.close();   // 只关响应，把连接还给池
        }
    }

    // 自定义 SOCKS 代理的 Socket 工厂（支持 HTTP）
    public static class SocksPlainSocketFactory extends PlainConnectionSocketFactory {
        private final Proxy proxy;

        public SocksPlainSocketFactory(Proxy proxy) {
            this.proxy = proxy;
        }

        @Override
        public Socket createSocket(HttpContext context) throws IOException {
            return new Socket(proxy);
        }
    }

    // 自定义 SOCKS 代理的 Socket 工厂（支持 HTTPS）
    public static class SocksSSLSocketFactory extends SSLConnectionSocketFactory {
        private final Proxy proxy;

        public SocksSSLSocketFactory(SSLContext sslContext, Proxy proxy) {
            super(sslContext, NoopHostnameVerifier.INSTANCE);
            this.proxy = proxy;
        }

        @Override
        public Socket createSocket(HttpContext context) throws IOException {
            return new Socket(proxy);
        }
    }


    private CloseableHttpClient createHttpClientProxy() throws Exception {
        // 创建 SOCKS 代理
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));

        // 创建 SSL 上下文
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial((chain, authType) -> true) // 信任所有证书
                .build();

        // 注册自定义的 Socket 工厂
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new HttpClientWithSocksProxy.SocksPlainSocketFactory(proxy))
                .register("https", new HttpClientWithSocksProxy.SocksSSLSocketFactory(sslContext, proxy))
                .build();

        // 设置连接池
        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(20);
        connectionManager.setDefaultMaxPerRoute(10);

        // 设置超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000)
                .setSocketTimeout(30000)
                .setConnectionRequestTimeout(30000)
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }


    private static OkHttpClient createAIHttpClientProxy(String proxyHost, int proxyPort)  {
        // 创建 SOCKS 代理
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));

        // 连接池配置
        ConnectionPool connectionPool = new ConnectionPool(
                20,  // 最大空闲连接数
                5,   // 保持存活时间（分钟）
                TimeUnit.MINUTES
        );

        // Cookie 管理
        CookieJar cookieJar = new CookieJar() {
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
                .proxy(proxy)
                .connectionPool(connectionPool)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .hostnameVerifier((hostname, session) -> true)
                .build();
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



    private  CloseableHttpClient createHttpClient() {
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



    private static Map<String, Boolean> buildFormDataFromMap(CloseableHttpClient httpClient, String cookie, String url, String novelNo) {
        Map<String, Boolean> mapAll = new LinkedHashMap<>();
        for (int i = 0;; i++) {
            Map<String, Boolean> map = new LinkedHashMap<>();
            int re = 0;
            while (map.isEmpty()) {
                map = getMaps(httpClient, cookie, url, novelNo, i, map);
                re+=1;
                if (re>5) {
                    System.out.println("没有获取到数据，正在重试：" + novelNo);
                    break;
                }
            }
            int beforeSize = mapAll.size();
            mapAll.putAll(map);
            int afterSize = mapAll.size();
            if (!(beforeSize < afterSize)) {
                break;
            }
        }
        return mapAll;
    }



    public UntilInfo getUntilInfo(String cookie,
                                  String username,
                                  Long userId) {
        boolean all = false;
        boolean stillValid = false;
        String deadline = "";

        final boolean proxyClient = Boolean.parseBoolean(
                dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("proxyClient").getValueField());
        final String url = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaAlarm").getValueField();

        try (CloseableHttpClient httpClient = proxyClient ? createHttpClientProxy() : createHttpClient()) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpGet.setHeader("Cookie", cookie);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    System.out.println("没有获取到页面:" + response.getStatusLine().getStatusCode());
                    return new UntilInfo(all, stillValid, deadline, userId, username);
                }

                String html = EntityUtils.toString(entity, StandardCharsets.UTF_8);

                /* ======  1. 成人认证  ====== */
                Document doc = Jsoup.parse(html);
                Elements adultImgs = doc.select(".switch-adult");
                if (!adultImgs.isEmpty()) {
                    all = "성인".equals(adultImgs.first().attr("alt"));
                }

                /* ======  2. 到期时间（直接正则抠 script）  ====== */
                Pattern p = Pattern.compile(
                        "mem_plus\\s*:\\s*\\{[^}]*enddate\\s*:\\s*['\"](\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})['\"]",
                        Pattern.DOTALL);
                Matcher m = p.matcher(html);
                if (m.find()) {
                    deadline = m.group(1);
                    LocalDateTime dTime = LocalDateTime.parse(deadline,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    stillValid = dTime.isAfter(LocalDateTime.now());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new UntilInfo(all, stillValid, deadline, userId, username);
    }

    public static Map<String, Boolean> getMaps(CloseableHttpClient httpClient, String cookie, String url, String novelNo, int i, Map<String, Boolean> map){
        try  {
            HttpPost httpPost = new HttpPost(String.format(url, novelNo, i));
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setHeader("Cookie", cookie);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    String htmlResponse = EntityUtils.toString(responseEntity, "UTF-8");

                    // 使用Jsoup解析HTML响应
                    Document doc = Jsoup.parse(htmlResponse);
                    Elements bookmarkElements = doc.select(".ep_style5");

                    for (Element element : bookmarkElements) {
                        if ((element.getElementsByClass("ep_style3").text().contains("공개예정"))) {
                            continue;
                        }
                        Elements select = element.select("i.ion-bookmark");
                        String id = select.get(0).id().replace("bookmark_", "");
                        Elements select1 = element.select("i.ion-image");
                        if (!select1.isEmpty()) {
                            map.put(id, true);
                        } else {
                            map.put(id, false);
                        }
                    }
                } else {
                    System.out.println("没有获取章节列表:" + response.getStatusLine().getStatusCode());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private boolean isInvalidHtml(String html) {
        return html == null || html.isEmpty();
    }

    public Map.Entry<String,String> extractWriterText(String html) {
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
        return new AbstractMap.SimpleEntry<>(authorId, authorName);
    }
    public String extractReadText(String html) {
        if (isInvalidHtml(html)) {
            return "";
        }

        Document doc = Jsoup.parse(html);

        // Select the second span within the first p tag inside a div with class "counter-line-a"
        Element secondSpan = doc.selectFirst("div.counter-line-a > p > span:nth-of-type(2)");

        if (secondSpan != null) {
            return secondSpan.text().trim();
        }

        return "";
    }

    public String extractLikeText(String html) {
        if (isInvalidHtml(html)) {
            return "0";
        }

        Document doc = Jsoup.parse(html);

        // 使用更精确的选择器，通过 btn-like 找到点赞数
        Element pElement = doc.selectFirst("div.icon-btn-area a.btn-like > p.bottom-txt");

        if (pElement != null) {
            return pElement.text().trim();
        }

        return "0";
    }

    private List<String> extractAllBadgeTexts(String html) {
        List<String> texts = new ArrayList<>();
        if (html == null || html.isEmpty()) {
            return texts;
        }

        Document doc = Jsoup.parse(html);
        Elements spans = doc.select("p.in-badge span"); // 所有匹配 span

        for (Element span : spans) {
            texts.add(span.text().trim());
        }
        return texts;
    }
    private  String sendGetRequest(CloseableHttpClient httpClient, String url, String cookie) throws Exception {
        HttpGet request = new HttpGet(url);
        if (cookie != null && !cookie.isEmpty()) {
            // 添加Cookie到请求头
            request.setHeader("Cookie", cookie);
        }

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String removeScriptsKeepText(String html) {
        Document doc = Jsoup.parse(html);
        doc.select("script").remove(); // 移除所有script标签

        // 关键配置：禁用实体转义
        doc.outputSettings()
                .escapeMode(Entities.EscapeMode.xhtml) // 使用最少转义
                .prettyPrint(false);                   // 禁用格式化

        // 直接获取body内的纯文本内容
        return doc.body().text();
    }


    private String extractContent(String jsonResponse, Pattern pattern, String episodeId) throws Exception {
        String contentAll = "";
//        jsonResponse = removeScriptsKeepText(jsonResponse);
        List<String> replace = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // 解析JSON
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // 获取list节点
            JsonNode listNode = rootNode.path("s");
            int listSize = listNode.size();
            int currentIndex = 1;

            // 遍历list中的每个元素
            for (JsonNode item : listNode) {
                // 提取episode_no和epi_cnt字段
                String content = item.path("text").asText();
                List<String> imgList = extractImgTags(content);
                extractHiddenPTags(content, replace);
                if (!imgList.isEmpty()) {
                    ChapterImageLink chapterImageLink = new ChapterImageLink(episodeId, imgList.get(0), String.format(imgIndex, currentIndex, listSize));
                    chapterImageLinkRepository.save(chapterImageLink);
                }
                Document document = Jsoup.parse(content);

                content = document.text();

                String cleanedLine = content.trim();

                Matcher matcher = pattern.matcher(cleanedLine);

                // 使用空字符串替换所有匹配项
                cleanedLine = matcher.replaceAll("").trim();

                content = cleanedLine + "\n"; // 获取纯文本内容

                contentAll += content;
                currentIndex += 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            contentAll += "获取章节失败";
        }

        for (String s : replace) {
            contentAll = contentAll.replaceAll(s, "");
        }
        contentAll = contentAll.replaceAll("(?m)^[\\s&&[^\n\r]]*$[\r\n]*", "").replaceAll("&nbsp;", " ").replaceAll("&lt;","<").replaceAll("&gt;",">");
        return contentAll;
    }

    private String extractContentError(String jsonResponse, Pattern pattern) throws Exception {
        String contentAll = "";

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // 解析JSON
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // 获取list节点
            JsonNode listNode = rootNode.path("s");
            // 遍历list中的每个元素
            for (JsonNode item : listNode) {
                // 提取episode_no和epi_cnt字段
                String content = item.path("text").asText();
                Document document = Jsoup.parse(content);

                content = document.text();

                String cleanedLine = content.trim();

                Matcher matcher = pattern.matcher(cleanedLine);

                // 使用空字符串替换所有匹配项
                cleanedLine = matcher.replaceAll("").trim();

                content = cleanedLine + "\n"; // 获取纯文本内容

                contentAll += content;
            }
        } catch (Exception e) {
            contentAll += "获取章节失败";
        }


        contentAll = contentAll.replaceAll("(?m)^[\\s&&[^\n\r]]*$[\r\n]*", "").replaceAll("&nbsp;", " ").replaceAll("&lt;","<").replaceAll("&gt;",">");
        return contentAll;
    }

    private List<String> translateSpans(List<String> spans) {
        Map<String, String> map = Map.of(
                "PLUS",   "PLUS",
                "독점",   "独家",
                "완결",   "完结",
                "연재중단", "连载中断",
                "자유", "自由",
                "챌린지", "挑战"
        );

        return spans.stream()
                .map(s -> map.getOrDefault(s.trim(), s)) // 找不到就保持原样
                .collect(Collectors.toList());
    }



    // 注入普通 HttpClient
    @Autowired
    @Qualifier("nonProxyHttpClient")
    private CloseableHttpClient nonProxyHttpClient;

    // 注入带代理的 HttpClient
    @Autowired
    @Qualifier("proxyHttpClient")
    private CloseableHttpClient proxyHttpClient;


    public boolean getNovelDetail(Long novelId) {
        final boolean executeDownload = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeDownload").getValueField());
        if (!executeDownload) {
            return false;
        }
        final String novelPiaDetail = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaDetail").getValueField();
        Novel referenceById = novelRepository.getReferenceById(novelId);
        if (referenceById.getPlatform().contains("upload")) {
            return false;
        }
//        final String cookie = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaCookie").getValueField();
        final String cookie = dictionaryService.getOneCookie();
        if (cookie == null || cookie.isEmpty()) {
            throw new RuntimeException("当前无可用的cookie");
        }
        final boolean proxyClient = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("proxyClient").getValueField());
        try(CloseableHttpClient httpClient = proxyClient ? proxyHttpClient : nonProxyHttpClient) {
            String s = sendGetRequest(httpClient, String.format(novelPiaDetail, referenceById.getTrueId()), cookie);
            assert s != null;
            if (s.contains("삭제된 소설 입니다")) {
                // 本小说已被删除
                return false;
            }
            String like = extractLikeText(s).replaceAll(",", "");
            String read = extractReadText(s).replaceAll(",", "");
            Map.Entry<String, String> stringStringEntry = extractWriterText(s);
            referenceById.setAuthorId(stringStringEntry.getKey());
            referenceById.setAuthorName(stringStringEntry.getValue());
            referenceById.setNovelLike(like.isEmpty()?0L : Long.parseLong(like));
            referenceById.setNovelRead(read.isEmpty()?0L : Long.parseLong(read));
            novelRepository.save(referenceById);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<ChapterExecute> executeDownloadOne(Long novelId) {
        final boolean executeDownload = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeDownload").getValueField());
        if (!executeDownload) {
            return null;
        }
        // 从数据库获取配置参数（这些配置在方法执行期间不变）
        System.out.println("开始下载");
        System.out.println("开始下载");
        final String regex = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaRegex").getValueField();
        final Pattern pattern = Pattern.compile(regex);
        final String unreleased = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaUnreleased").getValueField();
        final String unreleasedKeyWord = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaUnreleasedKeyWord").getValueField();
        final String getNovelChapter = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaGetNovelChapter").getValueField();
        final String epFormat = "EP%s";
        final String getPage = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaGetPage").getValueField();
//        final String cookie = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaCookie").getValueField();
        final String cookie = dictionaryService.getOneCookie();
        if (cookie == null || cookie.isEmpty()) {
            throw new RuntimeException("当前无可用的cookie");
        }
        final String novelPiaDetail = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaDetail").getValueField();

        final boolean novelPiaFont = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaFont").getValueField());

        List<Novel> novels = new ArrayList<>();
        Novel referenceById = novelRepository.getReferenceById(novelId);
        if (referenceById.getPlatform().contains("upload")) {
            return new ArrayList<>();
        }
        novels.add(referenceById);

        List<ChapterExecute> chapterExecuteList = Collections.synchronizedList(new ArrayList<>());

        // 创建线程池（根据实际情况调整核心线程数）
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService executor = Executors.newFixedThreadPool(corePoolSize);
        final boolean proxyClient = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("proxyClient").getValueField());
        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
        try(CloseableHttpClient httpClient = proxyClient ? createHttpClientProxy() : createHttpClient()) {
            List<CompletableFuture<Void>> futures = novels.stream()
                    .map(novel -> CompletableFuture.runAsync(() -> {
                        try {
                            boolean newChapterMessage = false;
                            if (downArraySetRun.contains(novel.getId())) {
                                return;
                            }
                            downArraySetRun.add(novel.getId());
                            String s = sendGetRequest(httpClient, String.format(novelPiaDetail, novel.getTrueId()), cookie);
                            if (s.contains("삭제된 소설 입니다")) {
                                // 本小说已被删除
                                return;
                            }
                            try {
                                SyosetuNovelDetail syosetuNovelDetail = saveNovelDetail(novel.getTrueId());
                                List<String> tagList = syosetuNovelDetail.getTagList();
                                for (String tag : tagList) {
                                    Tag tagToSave = new Tag(tag, "novelPia", tag);
                                    Optional<Tag> existingTag = tagRepository.findByPlatformAndTrueName("novelPia",tagToSave.getTrueName());
                                    if (existingTag.isEmpty()) {
                                        Tag syosetuTag = tagRepository.save(tagToSave);
                                        Optional<NovelTag> byNovelIdAndTagId = novelTagRepository.findByNovelIdAndTagId(novelId, syosetuTag.getId());
                                        if (byNovelIdAndTagId.isEmpty()) {
                                            novelTagRepository.save(new NovelTag(novelId, syosetuTag.getId()));
                                        }
                                    } else {
                                        Tag tag1 = existingTag.get();
                                        Optional<NovelTag> byNovelIdAndTagId = novelTagRepository.findByNovelIdAndTagId(novelId, tag1.getId());
                                        if (byNovelIdAndTagId.isEmpty()) {
                                            novelTagRepository.save(new NovelTag(novelId, tag1.getId()));
                                        }
                                    }
                                }
                                ChapterExecute chapterNumberAndIsDeletedFalse = chapterExecuteRepository.findByNovelIdAndChapterNumberAndIsDeletedFalse(novelId, 0);
                                if (chapterNumberAndIsDeletedFalse == null) {
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
                                    ChapterExecute save1 = chapterExecuteRepository.save(chapter);
                                }


                                String like = extractLikeText(s).replaceAll(",", "");
                                String read = extractReadText(s).replaceAll(",", "");
                                Map.Entry<String, String> stringStringEntry = extractWriterText(s);
                                novel.setAuthorId(stringStringEntry.getKey());
                                novel.setAuthorName(stringStringEntry.getValue());
                                novel.setNovelLike(like.isEmpty()?0L : Long.parseLong(like));
                                novel.setNovelRead(read.isEmpty()?0L : Long.parseLong(read));
                                List<String> spans = extractAllBadgeTexts(s);
                                List<String> strings = translateSpans(spans);
                                novel.setSpans(String.join(" ", strings));
                                novelRepository.save(novel);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
//                            Set<Integer> terminologyList = terminologyService.findDistinctChapterNumbersByNovelIdAndSourceTargetDownloaded(novel.getId());

                            List<String> titlesByNovelIdAndIsDeletedFalse = chapterRepository.findTitlesByNovelIdAndIsDeletedFalse(novel.getId());
                            List<String> executingChapters = chapterExecuteRepository
                                    .findTitlesByNovelIdAndIsDeletedFalse(novel.getId());
                            executingChapters.addAll(titlesByNovelIdAndIsDeletedFalse);
                            Set<String> existingChaptersSet = new HashSet<>(executingChapters);
//                            Set<String> existingChaptersSet = new HashSet<>();
                            // 获取章节映射关系
                            Map<String, Boolean> episodeMap = buildFormDataFromMap(
                                    httpClient, cookie, getPage, novel.getTrueId()
                            );
                            for (int u = 0;u<4;u++) {
                                if (!episodeMap.isEmpty()) {
                                    continue;
                                }
                                episodeMap = buildFormDataFromMap(
                                        httpClient, cookie, getPage, novel.getTrueId()
                                );
                            }
//                            System.out.println("episodeMap:"+episodeMap.size() + " novel.getTrueId():" + novel.getTrueId());
                            int episodeCounter = 1;
                            for (Map.Entry<String, Boolean> entry : episodeMap.entrySet()) {
                                String episodeId = entry.getKey();
                                boolean photo = entry.getValue();
                                String episodeFile = String.format(epFormat, String.format("%04d", episodeCounter));

                                // 检查章节是否需要处理
                                if (!existingChaptersSet.contains(episodeFile)) {
                                    System.out.println("章节需要处理");
                                    newChapterMessage = true;
                                    try {
                                        // 获取章节内容
                                        String jsonResponse = sendGetRequest(
                                                httpClient,
                                                String.format(getNovelChapter, episodeId),
                                                cookie
                                        );
                                        String content = extractContent(jsonResponse, pattern, episodeId);

                                        // 处理未发布章节
                                        if (content.trim().isEmpty()) {
                                            System.out.println("处理未发布章节");
                                            String unreleasedResponse = sendGetRequest(
                                                    httpClient,
                                                    String.format(unreleased, episodeId),
                                                    cookie
                                            );
                                            if (unreleasedResponse.contains(unreleasedKeyWord)) {
                                                continue;
                                            }
                                        }

                                        content = content.replaceAll("V1Zn[A-Za-z0-9+=]+", "");
                                        if (novelPiaFont) {
                                            content = FontDeobfuscatorTool.deobfuscate(content);
                                        }
                                        // 保存章节执行记录
                                        ChapterExecute chapter = new ChapterExecute(
                                                novel.getId(),
                                                episodeFile,
                                                episodeCounter,
                                                content,
                                                0,
                                                episodeId,
                                                photo
                                        );
                                        ChapterExecute saved = chapterExecuteRepository.save(chapter);
                                        // 在这里获取图片位置，并取出图片链接

                                        // 添加同步锁保证控制台输出有序性
                                        synchronized (System.out) {
                                            System.out.println("已保存：" + novel.getTitle() + "--" + saved.getTitle());
                                        }

                                        chapterExecuteList.add(saved);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                episodeCounter += 1;
                            }
                            if (newChapterMessage) {
                                //novelId
                                Set<Long> userIds = favoriteRepository.findByObjectId(novelId)
                                        .stream()
                                        .map(UserIdProjection::getUserId)
                                        .collect(Collectors.toSet());
                                messageService.newChapterMessage(userIds, referenceById);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            downArraySetRun.remove(novel.getId());
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


    public List<ChapterExecute> executeDownload() {
        // 从数据库获取配置参数（这些配置在方法执行期间不变）
        System.out.println("开始下载");
//        System.out.println("开始下载");
//        System.out.println("开始下载");
//        System.out.println("开始下载");
//        System.out.println("开始下载");
//        System.out.println("开始下载");
        final String regex = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaRegex").getValueField();
        final Pattern pattern = Pattern.compile(regex);
        final String unreleased = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaUnreleased").getValueField();
        final String unreleasedKeyWord = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaUnreleasedKeyWord").getValueField();
        final String getNovelChapter = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaGetNovelChapter").getValueField();
        final String epFormat = "EP%s";
        final String getPage = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaGetPage").getValueField();
//        final String cookie = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaCookie").getValueField();
        final String cookie = dictionaryService.getOneCookie();
        if (cookie == null || cookie.isEmpty()) {
            throw new RuntimeException("当前无可用的cookie");
        }
        final String novelPiaDetail = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaDetail").getValueField();

        final boolean novelPiaFont = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaFont").getValueField());

        List<Novel> novels = getNovels();
//        List<Novel> novels = new ArrayList<>();
//        Novel novel1 = new Novel();
//        novel1.setTrueId("24");
//        novel1.setId(350124L);
////        novel1.setTitle("데이팅 어플을 깔았더니 이세계 처녀들이 집착한다");
//        novels.add(novel1);
//
//
//        Novel novel2 = new Novel();
//        novel2.setTrueId("285100");
//        novel2.setId(350426L);
//        novels.add(novel2);
//
//        Novel novel3 = new Novel();
//        novel3.setTrueId("14124");
//        novel3.setId(350125L);
//        novels.add(novel3);
//
//        Novel novel5 = new Novel();
//        novel5.setTrueId("119950");
//        novel5.setId(350057L);
//        novels.add(novel5);
//
//        Novel novel6 = new Novel();
//        novel6.setTrueId("276880");
//        novel6.setId(350015L);
//        novels.add(novel6);
//
//        Novel novel7 = new Novel();
//        novel7.setTrueId("337468");
//        novel7.setId(350100L);
//        novels.add(novel7);
//
//        Novel novel8 = new Novel();
//        novel8.setTrueId("285101");
//        novel8.setId(350350L);
//        novels.add(novel8);
//
//        Novel novel9 = new Novel();
//        novel9.setTrueId("268987");
//        novel9.setId(350014L);
//        novels.add(novel9);
//
//
//        Novel novel10 = new Novel();
//        novel10.setTrueId("225294");
//        novel10.setId(349948L);
//        novels.add(novel10);
//
//
//        Novel novel11 = new Novel();
//        novel11.setTrueId("232317");
//        novel11.setId(349955L);
//        novels.add(novel11);


        List<ChapterExecute> chapterExecuteList = Collections.synchronizedList(new ArrayList<>());

        // 创建线程池（根据实际情况调整核心线程数）
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService executor = Executors.newFixedThreadPool(corePoolSize);
        final boolean proxyClient = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("proxyClient").getValueField());
        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
        try(CloseableHttpClient httpClient = proxyClient ? createHttpClientProxy() : createHttpClient()) {
            List<CompletableFuture<Void>> futures = novels.stream()
                    .map(novel -> CompletableFuture.runAsync(() -> {
                        try {
                            if (downArraySetRun.contains(novel.getId())) {
                                return;
                            }
                            downArraySetRun.add(novel.getId());
                            String s = sendGetRequest(httpClient, String.format(novelPiaDetail, novel.getTrueId()), cookie);
                            if (s.contains("삭제된 소설 입니다")) {
                                // 本小说已被删除
                                return;
                            }

//                            Set<Integer> terminologyList = terminologyService.findDistinctChapterNumbersByNovelIdAndSourceTargetDownloaded(novel.getId());

                            List<String> titlesByNovelIdAndIsDeletedFalse = chapterRepository.findTitlesByNovelIdAndIsDeletedFalse(novel.getId());
                            List<String> executingChapters = chapterExecuteRepository
                                    .findTitlesByNovelIdAndIsDeletedFalse(novel.getId());
                            executingChapters.addAll(titlesByNovelIdAndIsDeletedFalse);
                            Set<String> existingChaptersSet = new HashSet<>(executingChapters);
//                            Set<String> existingChaptersSet = new HashSet<>();
                            // 获取章节映射关系
                            Map<String, Boolean> episodeMap = buildFormDataFromMap(
                                    httpClient, cookie, getPage, novel.getTrueId()
                            );
                            for (int u = 0;u<4;u++) {
                                if (!episodeMap.isEmpty()) {
                                    continue;
                                }
                                episodeMap = buildFormDataFromMap(
                                        httpClient, cookie, getPage, novel.getTrueId()
                                );
                            }
                            System.out.println("episodeMap:"+episodeMap.size() + " novel.getTrueId():" + novel.getTrueId());
                            int episodeCounter = 1;
                            for (Map.Entry<String, Boolean> entry : episodeMap.entrySet()) {
                                String episodeId = entry.getKey();
                                boolean photo = entry.getValue();
                                String episodeFile = String.format(epFormat, String.format("%04d", episodeCounter));

                                // 检查章节是否需要处理
                                if (!existingChaptersSet.contains(episodeFile)) {
                                    System.out.println("章节需要处理");
                                    try {
                                        // 获取章节内容
                                        String jsonResponse = sendGetRequest(
                                                httpClient,
                                                String.format(getNovelChapter, episodeId),
                                                cookie
                                        );
                                        String content = extractContent(jsonResponse, pattern, episodeId);

                                        // 处理未发布章节
                                        if (content.trim().isEmpty()) {
                                            System.out.println("处理未发布章节");
                                            String unreleasedResponse = sendGetRequest(
                                                    httpClient,
                                                    String.format(unreleased, episodeId),
                                                    cookie
                                            );
                                            if (unreleasedResponse.contains(unreleasedKeyWord)) {
                                                continue;
                                            }
                                        }

                                        content = content.replaceAll("V1Zn[A-Za-z0-9+=]+", "");
                                        if (novelPiaFont) {
                                            content = FontDeobfuscatorTool.deobfuscate(content);
                                        }
                                        // 保存章节执行记录
                                        ChapterExecute chapter = new ChapterExecute(
                                                novel.getId(),
                                                episodeFile,
                                                episodeCounter,
                                                content,
                                                0,
                                                episodeId,
                                                photo
                                        );
                                        ChapterExecute saved = chapterExecuteRepository.save(chapter);
                                        // 在这里获取图片位置，并取出图片链接

                                        // 添加同步锁保证控制台输出有序性
                                        synchronized (System.out) {
                                            System.out.println("已保存：" + novel.getTitle() + "--" + saved.getTitle());
                                        }

                                        chapterExecuteList.add(saved);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                episodeCounter += 1;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            downArraySetRun.remove(novel.getId());
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


    private void processSingleChapter(List<ChapterExecute> chapters, String platformName,
                                                String apiUrl, String model, List<PlatformApiKey> apiKeys, String aiPrompt, OkHttpClient httpClient) {
        if (chapters.isEmpty()) {
            return;
        }
        Novel novel = novelRepository.findByIdAndIsDeletedFalse(chapters.get(0).getNovelId());

        try {
            for (ChapterExecute chapter : chapters) {
                chapter.setContent(ShuTuNovelChapterService.processText(chapter.getContent()));
                try {
                    // 使用线程安全的随机数
                    ThreadLocalRandom random = ThreadLocalRandom.current();
//                    chapter.setTranslatorContent(chapter.getTranslatorContent().replaceAll("V1Zn[A-Za-z0-9+=]+", ""));

                    // 状态检查和初始化
                    if (chapter.getNowState() == 3) continue;
                    ValidationResult result = panduanyichang(chapter.getTranslatorContent());
                    try {
                        // 更新状态需要同步
                        synchronized (chapter) {
                            chapter.setNowState(3);
                            chapterExecuteRepository.save(chapter);
                        }

                        // 处理异常内容
                        for (Integer index : result.abnormalIndices) {
                            List<Terminology> terminologyList = terminologyService.findAllByNovelId(chapters.get(0).getNovelId());
                            Set<String> terminologyListSourceNames = terminologyList.stream()
                                    .map(Terminology::getSourceName)
                                    .collect(Collectors.toSet());
                            List<Terminology> terminologiesUse = new ArrayList<>();
                            String content = chapter.getTranslatorContent();
                            terminologyList.forEach(terminology -> {
                                if (content.contains(terminology.getSourceName())) {
                                    terminologiesUse.add(terminology);
                                }
                            });
                            Map<String, String> terminologyMap = terminologiesUse.stream()
                                    .collect(Collectors.toMap(
                                            Terminology::getSourceName,
                                            Terminology::getTargetName,
                                            (existingValue, newValue) -> newValue
                                    ));
                            String terminologyJson = new Gson().toJson(terminologyMap);
                            String aiPromptReplace = aiPrompt.replace("<原术语表>", terminologyJson);
                            processAbnormalSegment(novel, terminologyMap,terminologyList,terminologyListSourceNames,result, index, apiKeys.get(random.nextInt(apiKeys.size())), apiUrl, model, chapter, aiPromptReplace, httpClient,platformName);
                        }
                    }finally {
                        // 更新最终状态
                        synchronized (chapter) {
                            chapter.setTranslatorContent(String.join("\n", result.parts));
                            chapter.setNowState(chapter.getNowState() == 3 ? 2 : 1);
                            chapterExecuteRepository.save(chapter);
                            // 保存最终章节
                            if (chapter.getNowState() == 2) {
                                saveFinalChapter(chapter);
                            }
                        }
                    }
                } catch (Exception e) {
                    handleChapterError(chapter, e);
                }
            }
        } finally {
            copyOnWriteArraySetExceptionRun.remove(novel.getId());
            uploadCopyOnWriteArraySetExceptionRun.remove(novel.getId());
        }
    }

    private String processAbnormalSegment(Novel novel,Map<String, String> terminologyMap,List<Terminology> terminologyList,Set<String> terminologyListSourceNames,ValidationResult result, int index,
                                        PlatformApiKey apiKey, String apiUrl,
                                        String model, ChapterExecute chapter, String aiPrompt, OkHttpClient httpClient, String platformName) {
        String original = result.parts.get(index);
        StringBuilder content = new StringBuilder();

        try {
            String translation = translation(platformName, apiUrl, original, model, true, aiPrompt, httpClient);
            String s = content.append(translation).toString();
            ObjectMapper mapper = new ObjectMapper();
            // 直接映射到 JsonData 对象
//            System.out.println("===");
//            System.out.println(s);
//            System.out.println("===");
            JsonData data;
            try {
                if (Objects.equals(translation, "无需汉化0ak2")) {
                    data = new JsonData();
                    data.setTable(new HashMap<>());
                    data.setTranslation("");
                } else {
                    data = mapper.readValue(extractJson(s), JsonData.class);
                }
            } catch (JsonMappingException | JsonParseException e) {
                Map<String, String> table = JsonEscapeUtils.getTable(translation);
                String translation1 = JsonEscapeUtils.getTranslation(translation);
                if (!translation1.isEmpty()) {
                    data = new JsonData();
                    data.setTable(table);
                    data.setTranslation(translation1);
                } else {
                    throw new RuntimeException();
                }
            }
            result.parts.set(index, data.getTranslation());
            terminologyMap.putAll(data.getTable());
            List<String> list = terminologyList.stream().map(Terminology::getSourceName).toList();
            Set<String> set = new HashSet<>(list);

            if (!data.getTable().isEmpty()) {
                Map<String, String> filteredTerminologyMap = data.getTable().entrySet().stream()
                        .filter(entry -> !terminologyListSourceNames.contains(entry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                for (Map.Entry<String, String> table : filteredTerminologyMap.entrySet()) {
                    if (set.contains(table.getKey())) continue;
                    Terminology terminology = new Terminology(chapter.getNovelId(), novel.getTrueId(), chapter.getTrueId(), table.getKey(), table.getValue(), chapter.getChapterNumber());
                    terminologyService.save(terminology);
                }
            }
        } catch (Exception e) {
            String s = buildAbnormalContent(original);
            result.parts.set(index, s);
            chapter.setNowState(1);
            e.printStackTrace();
        }
        return content.toString();
    }

    private boolean isValidTranslation(String translation, String original) {
        return translation.length() >= original.length() / 2 && !original.isEmpty();
    }

    private void buildAbnormalContent(StringBuilder builder, String original) {
        builder.append("\n下方翻译出现异常！\n")
                .append(original)
                .append("\n上方翻译出现异常！\n");
    }
    private String buildAbnormalContent(String original) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n下方翻译出现异常！\n")
                .append(original)
                .append("\n上方翻译出现异常！\n");
        return builder.toString();
    }

    private synchronized void saveFinalChapter(ChapterExecute chapterExecute) {
        String obfuscator = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("TextObfuscator").getValueField();
        Chapter isExist = chapterRepository.findByNovelIdAndChapterNumberAndIsDeletedFalse(chapterExecute.getNovelId(), chapterExecute.getChapterNumber());
        if (isExist != null) {
            int length = isExist.getContent().length();
            isExist.setChapterNumber(chapterExecute.getChapterNumber());
            String result = TextObfuscator.insertObfuscatedText(chapterExecute.getTranslatorContent(), obfuscator);
            isExist.setContent(result);
            isExist.setTitle(chapterExecute.getTitle());
            isExist.setNovelId(chapterExecute.getNovelId());
            isExist.setTrueId(chapterExecute.getTrueId());
            isExist.setOwnPhoto(chapterExecute.isOwnPhoto());
            novelRepository.incrementFontNumberById(isExist.getNovelId(), (long) (isExist.getContent().length() - length));
            chapterRepository.save(isExist);
            chapterSyncService.save(isExist);
        } else {
            Chapter chapter = new Chapter();
            chapter.setChapterNumber(chapterExecute.getChapterNumber());
            String result = TextObfuscator.insertObfuscatedText(chapterExecute.getTranslatorContent(), obfuscator);
            chapter.setContent(result);
            chapter.setTitle(chapterExecute.getTitle());
            chapter.setNovelId(chapterExecute.getNovelId());
            chapter.setTrueId(chapterExecute.getTrueId());
            chapter.setOwnPhoto(chapterExecute.isOwnPhoto());
            novelRepository.incrementFontNumberById(chapter.getNovelId(), (long) chapter.getContent().length());
            chapterRepository.save(chapter);
            chapterSyncService.save(chapter);
        }
    }

    private synchronized void handleChapterError(ChapterExecute chapter, Exception e) {
        chapter.setNowState(1);
        chapterExecuteRepository.save(chapter);
        e.printStackTrace();
    }

    public void executeTranslationException(String platformName, List<ChapterExecute> chapterExecuteList, int poolSize) {
        System.out.println("========开始执行异常=========");
        System.out.println("========开始执行异常=========");
        System.out.println(chapterExecuteList.size());
        System.out.println("========开始执行异常=========");
        System.out.println("========开始执行异常=========");
        // 预加载公共配置（线程安全方式）
        String apiUrl = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse(platformName).getValueField();
        String model = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflowModel_1").getValueField();
        String aiPrompt = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("aiPrompt").getValueField();
        Platform platform = platformRepository.findPlatformByPlatformName(platformName);
        List<PlatformApiKey> apiKeys = platformApiKeyRepository.findByPlatformIdAndIsDeletedFalse(platform.getId());
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
//        OkHttpClient httpClient = createOKHttpClient();
        final boolean AIproxyClient = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("AIproxyClient").getValueField());
        OkHttpClient httpClient = AIproxyClient ? createAIHttpClientProxy(proxyHost, proxyPort) : createOKHttpClient();
        try {
            Map<Long, List<ChapterExecute>> groupedByNovelId = chapterExecuteList.stream()
                    .collect(Collectors.groupingBy(
                            ChapterExecute::getNovelId,
                            Collectors.mapping(
                                    ce -> ce,
                                    Collectors.toList()
                            )
                    ))
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().stream()
                                    .sorted(Comparator.comparingInt(ChapterExecute::getChapterNumber))
                                    .collect(Collectors.toList())
                    ));

            // 并行处理每个章节
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Map.Entry<Long, List<ChapterExecute>> entry : groupedByNovelId.entrySet()) {
                List<ChapterExecute> chapters = entry.getValue();
                futures.add(CompletableFuture.runAsync(() ->
                        processSingleChapter(chapters, platformName, apiUrl, model, apiKeys, aiPrompt, httpClient),
                        executor));
            }
            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Processing interrupted", e);
            }
        } finally {
            executor.shutdown();
//             销毁 OkHttpClient 实例
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }

    }


    public List<ChapterErrorExecute> executeTranslationError(String platformName, List<ChapterErrorExecute> chapterErrorExecuteList) {
        // 初始化配置信息（保持单线程获取）
        String siliconflowApiUrl = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse(platformName).getValueField();
        String siliconflowModel = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflowModel_1").getValueField();
        String aiPrompt = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("aiPrompt").getValueField();
        String siliconflowMaxLength = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflowMaxLength").getValueField();
        Platform platform = platformRepository.findPlatformByPlatformName(platformName);
        List<PlatformApiKey> apiKeys = platformApiKeyRepository.findByPlatformIdAndIsDeletedFalse(platform.getId());
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
//        OkHttpClient httpClient = createOKHttpClient();
        final boolean AIproxyClient = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("AIproxyClient").getValueField());
        OkHttpClient httpClient = AIproxyClient ? createAIHttpClientProxy(proxyHost, proxyPort) : createOKHttpClient();
        // 使用线程安全集合保存结果
        List<ChapterErrorExecute> chapterExecuteOverList = Collections.synchronizedList(new ArrayList<>());
        try {

            Map<Long, List<ChapterErrorExecute>> groupedByNovelId = chapterErrorExecuteList.stream()
                    .collect(Collectors.groupingBy(
                            ChapterErrorExecute::getNovelId,
                            Collectors.mapping(
                                    ce -> ce,
                                    Collectors.toList()
                            )
                    ))
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().stream()
                                    .sorted(Comparator.comparingInt(ChapterErrorExecute::getChapterNumber))
                                    .collect(Collectors.toList())
                    ));


            // 并行处理每个章节
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Map.Entry<Long, List<ChapterErrorExecute>> entry : groupedByNovelId.entrySet()) {
                List<ChapterErrorExecute> chapters = entry.getValue();
                futures.add(CompletableFuture.runAsync(() ->
                                processChapterError(chapters, apiKeys, siliconflowApiUrl,
                                        siliconflowModel, siliconflowMaxLength, chapterExecuteOverList, aiPrompt, httpClient,platformName),
                        executor));
            }

//
//            List<CompletableFuture<Void>> futures = chapterErrorExecuteList.stream()
//                    .filter(chapter -> chapter.getNowState() != 3)
//                    .map(chapter -> CompletableFuture.runAsync(() ->
//                                    processChapterError(chapter, apiKeys, siliconflowApiUrl,
//                                            siliconflowModel, siliconflowMaxLength, chapterExecuteOverList,aiPrompt,httpClient),
//                            executor))
//                    .toList();

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

    private void processChapterError(List<ChapterErrorExecute> chapterExecutes, List<PlatformApiKey> apiKeys,
                                String apiUrl, String model, String maxLengthStr,
                                List<ChapterErrorExecute> resultList, String aiPrompt, OkHttpClient httpClient,String platformName) {
        // 每个线程使用独立的随机数生成器
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int maxLength = Integer.parseInt(maxLengthStr);
        if (chapterExecutes.isEmpty()) {
            return;
        }
        Novel novel = novelRepository.findByIdAndIsDeletedFalse(chapterExecutes.get(0).getNovelId());
        for (ChapterErrorExecute chapterExecute : chapterExecutes) {
            chapterExecute.setContent(ShuTuNovelChapterService.processText(chapterExecute.getContent()));
            StringBuilder okContent = new StringBuilder();
            boolean hasError = false;
            try {
                // 状态预检查
                if (chapterExecute.getNowState() == 3) continue;

                chapterExecute.setContent(RemoveRepeatUtil.processString(chapterExecute.getContent()));

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
                    try {
                        List<Terminology> terminologyList = terminologyService.findAllByNovelId(chapterExecutes.get(0).getNovelId());
                        Set<String> terminologyListSourceNames = terminologyList.stream()
                                .map(Terminology::getSourceName)
                                .collect(Collectors.toSet());
                        List<Terminology> terminologiesUse = new ArrayList<>();
                        String content = chapterExecute.getContent();
                        terminologyList.forEach(terminology -> {
                            if (content.contains(terminology.getSourceName())) {
                                terminologiesUse.add(terminology);
                            }
                        });
                        Map<String, String> terminologyMap = terminologiesUse.stream()
                                .collect(Collectors.toMap(
                                        Terminology::getSourceName,
                                        Terminology::getTargetName,
                                        (existingValue, newValue) -> newValue
                                ));
                        String terminologyJson = new Gson().toJson(terminologyMap);
                        String aiPromptReplace = aiPrompt.replace("<原术语表>", terminologyJson);
                        part = processMultilineString(part);
                        String translation = translation(platformName, apiUrl, part, model, true, aiPromptReplace, httpClient);
                        ObjectMapper mapper = new ObjectMapper();
                        // 直接映射到 JsonData 对象
                        JsonData data;
                        try {
                            if (Objects.equals(translation, "无需汉化0ak2")) {
                                data = new JsonData();
                                data.setTable(new HashMap<>());
                                data.setTranslation("");
                            } else {
                                data = mapper.readValue(extractJson(translation), JsonData.class);
                            }
                        } catch (JsonMappingException | JsonParseException e) {
                            Map<String, String> table = JsonEscapeUtils.getTable(translation);
                            String translation1 = JsonEscapeUtils.getTranslation(translation);
                            if (!translation1.isEmpty()) {
                                data = new JsonData();
                                data.setTable(table);
                                data.setTranslation(translation1);
                            } else {
                                throw new RuntimeException();
                            }
                        }
                        okContent.append(data.getTranslation()).append(System.lineSeparator());
                        terminologyMap.putAll(data.getTable());
                        List<String> list = terminologyList.stream().map(Terminology::getSourceName).toList();
                        Set<String> set = new HashSet<>(list);
                        if (!data.getTable().isEmpty()) {
                            Map<String, String> filteredTerminologyMap = data.getTable().entrySet().stream()
                                    .filter(entry -> !terminologyListSourceNames.contains(entry.getKey()))
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                            for (Map.Entry<String, String> table : filteredTerminologyMap.entrySet()) {
                                if (set.contains(table.getKey())) continue;
                                Terminology terminology = new Terminology(chapterExecute.getNovelId(), novel.getTrueId(), chapterExecute.getTrueId(), table.getKey(), table.getValue(), chapterExecute.getChapterNumber());
                                terminologyService.save(terminology);
                            }
                        }
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
                        String obfuscator = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("TextObfuscator").getValueField();
                        Chapter chapter = chapterRepository.findByNovelIdAndChapterNumberAndIsDeletedFalse(chapterExecute.getNovelId(), chapterExecute.getChapterNumber());
                        if (chapter != null) {
                            int length = chapter.getContent().length();
                            chapter.setChapterNumber(chapterExecute.getChapterNumber());
                            String result = TextObfuscator.insertObfuscatedText(chapterExecute.getTranslatorContent(), obfuscator);
                            chapter.setContent(result);
                            chapter.setTitle(chapterExecute.getTitle());
                            chapter.setNovelId(chapterExecute.getNovelId());
                            chapter.setTrueId(chapterExecute.getTrueId());
                            chapter.setOwnPhoto(chapterExecute.isOwnPhoto());
                            novelRepository.incrementFontNumberById(chapter.getNovelId(), (long) (chapter.getContent().length() - length));
                            chapterRepository.save(chapter);
                            chapterSyncService.save(chapter);
                            userFeedbackRepository.softDeleteByUserAndContent(chapterExecute.getNovelId(), chapter.getId());
                            chapterExecute.setDeleted(true);
                            chapterErrorExecuteRepository.softDeleteById(chapterExecute.getId());
                        } else {
                            Chapter chapter1 = new Chapter();
                            chapter1.setChapterNumber(chapterExecute.getChapterNumber());
                            String result = TextObfuscator.insertObfuscatedText(chapterExecute.getTranslatorContent(), obfuscator);
                            chapter1.setContent(result);
                            chapter1.setTitle(chapterExecute.getTitle());
                            chapter1.setNovelId(chapterExecute.getNovelId());
                            chapter1.setTrueId(chapterExecute.getTrueId());
                            chapter1.setOwnPhoto(chapterExecute.isOwnPhoto());
                            novelRepository.incrementFontNumberById(chapter1.getNovelId(), (long) chapter1.getContent().length());
                            chapterRepository.save(chapter1);
                            chapterSyncService.save(chapter1);
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
    }



    public ChapterExecute executeTranslation(String platformName, ChapterExecute chapterExecute, OkHttpClient httpClient) {
        if (chapterExecute.getNowState() == 3) {
            return chapterExecute;
        }
        chapterExecute.setNowState(3);
        chapterExecuteRepository.save(chapterExecute);
        String siliconflowApiUrl = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflow").getValueField();
        String siliconflowModel = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflowModel_1").getValueField();
        String aiPrompt = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("aiPrompt").getValueField();
        Platform platform = platformRepository.findPlatformByPlatformName(platformName);
        Random random = new Random();
        List<PlatformApiKey> apiKeys = platformApiKeyRepository.findByPlatformIdAndIsDeletedFalse(platform.getId());
        String translatorContent = chapterExecute.getTranslatorContent();
        ValidationResult panduanyichang = panduanyichang(translatorContent);
        for (Integer abnormalIndex : panduanyichang.abnormalIndices) {
            StringBuilder okContent = new StringBuilder();
            PlatformApiKey platformApiKey = apiKeys.get(random.nextInt(apiKeys.size()));
            String test = panduanyichang.parts.get(abnormalIndex);
            try {
                String translation = translation(platformApiKey.getApiKey(), siliconflowApiUrl, test, siliconflowModel, true, aiPrompt, httpClient);
                if (translation.length() < test.length()/2 && !test.isEmpty()) {
                    okContent.append("\n下方翻译出现异常！\n").append(test).append("\n上方翻译出现异常！\n");
                    panduanyichang.parts.set(abnormalIndex, okContent.toString());
                    chapterExecute.setNowState(1);
                    continue;
                }
                okContent.append(translation);
                panduanyichang.parts.set(abnormalIndex, okContent.toString());
            } catch (Exception e) {
                okContent.append("\n下方翻译出现异常！\n").append(test).append("\n上方翻译出现异常！\n");
                panduanyichang.parts.set(abnormalIndex, okContent.toString());
                chapterExecute.setNowState(1);
                e.printStackTrace();
            }
        }


        if (chapterExecute.getNowState() != 1) {
            chapterExecute.setNowState(2);
        }
        chapterExecute.setTranslatorContent(String.join("\n", panduanyichang.parts));
        chapterExecuteRepository.save(chapterExecute);
        return chapterExecute;
    }


    public List<ChapterExecute> executeTranslation(String platformName, List<ChapterExecute> chapterExecuteList,int poolSize) {
        System.out.println("=======开始执行翻译=======");
        System.out.println("=======开始执行翻译=======");
        System.out.println("chapterExecuteList.size()：" + chapterExecuteList.size());
        System.out.println("poolSize：" + poolSize);
        System.out.println("=======开始执行翻译=======");
        System.out.println("=======开始执行翻译=======");
        // 初始化配置信息（保持单线程获取）
        String siliconflowApiUrl = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse(platformName).getValueField();
        String siliconflowModel = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflowModel_1").getValueField();
        String aiPrompt = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("aiPrompt").getValueField();
        String siliconflowMaxLength = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflowMaxLength").getValueField();
        Platform platform = platformRepository.findPlatformByPlatformName(platformName);
        List<PlatformApiKey> apiKeys = platformApiKeyRepository.findByPlatformIdAndIsDeletedFalse(platform.getId());
        // 使用线程安全集合保存结果
        List<ChapterExecute> chapterExecuteOverList = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        final boolean AIproxyClient = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("AIproxyClient").getValueField());
        OkHttpClient httpClient = AIproxyClient ? createAIHttpClientProxy(proxyHost, proxyPort) : createOKHttpClient();
//        OkHttpClient httpClient = createOKHttpClient();
        try {
            Map<Long, List<ChapterExecute>> groupedByNovelId = chapterExecuteList.stream()
                    .collect(Collectors.groupingBy(
                            ChapterExecute::getNovelId,
                            Collectors.mapping(
                                    ce -> ce,
                                    Collectors.toList()
                            )
                    ))
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().stream()
                                    .sorted(Comparator.comparingInt(ChapterExecute::getChapterNumber))
                                    .collect(Collectors.toList())
                    ));

            // 并行处理每个章节
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            System.out.println("groupedByNovelId：" + groupedByNovelId.size());
            for (Map.Entry<Long, List<ChapterExecute>> entry : groupedByNovelId.entrySet()) {
                List<ChapterExecute> chapters = entry.getValue();
                futures.add(CompletableFuture.runAsync(() ->
                                processChapter(chapters, apiKeys, siliconflowApiUrl,
                                        siliconflowModel, siliconflowMaxLength, chapterExecuteOverList, aiPrompt, httpClient,platformName),
                        executor));
            }

            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // 销毁 OkHttpClient 实例
//            httpClient.dispatcher().executorService().shutdown();
//            httpClient.connectionPool().evictAll();
            executor.shutdown();
        }

        return chapterExecuteOverList;
    }

    public static void extractHiddenPTags(String content, List<String> replace) {
        Pattern pattern = Pattern.compile(regex1, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            replace.add(matcher.group(1)); // group(1) 就是捕获的内部文本
        }
    }

    public static List<String> extractImgTags(String content) {
        List<String> imgTags = new ArrayList<>();
        // 定义正则表达式匹配 <img> 标签
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            imgTags.add(matcher.group());
        }
        return imgTags;
    }

    /**
     * 从输入字符串中提取唯一的目标 JSON 对象。
     *
     * @param input 包含 JSON 的原始字符串
     * @return 提取出的 JSON 字符串，若未找到或格式不正确则返回 null
     */
    public static String extractJson(String input) {
        if (input == null) return null;
        String key = "\"table\"";
        int tableIdx = input.indexOf(key);
        if (tableIdx < 0) {
            return null; // 未找到 "table"
        }
        // 找到 "table" 之前最近的 '{'
        int start = input.lastIndexOf('{', tableIdx);
        if (start < 0) {
            return null;
        }
        int depth = 0;
        for (int i = start; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    // 截取完整 JSON
                    return input.substring(start, i + 1);
                }
            }
        }
        return null; // 未匹配完整
    }

    private void processChapter(List<ChapterExecute> chapterExecutes, List<PlatformApiKey> apiKeys,
                                String apiUrl, String model, String maxLengthStr,
                                List<ChapterExecute> resultList, String aiPrompt, OkHttpClient httpClient,String platformName) {
        // 每个线程使用独立的随机数生成器
        System.out.println("开始执行本小说汉化");
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int maxLength = Integer.parseInt(maxLengthStr);
        Novel novel = novelRepository.findByIdAndIsDeletedFalse(chapterExecutes.get(0).getNovelId());
        if (chapterExecutes.isEmpty()) {
            copyOnWriteArraySetRun.remove(novel.getId());
            uploadCopyOnWriteArraySetRun.remove(novel.getId());
            return;
        }
        try {
            for (ChapterExecute chapterExecute : chapterExecutes) {
                chapterExecute.setContent(ShuTuNovelChapterService.processText(chapterExecute.getContent()));
                boolean hasError = false;

                try {
                    StringBuilder okContent = new StringBuilder();
                    // 状态预检查
                    if (chapterExecute.getNowState() == 3) continue;

                    chapterExecute.setContent(RemoveRepeatUtil.processString(chapterExecute.getContent()));

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
                        try {
                            List<Terminology> terminologyList = terminologyService.findAllByNovelId(chapterExecutes.get(0).getNovelId());
                            Set<String> terminologyListSourceNames = terminologyList.stream()
                                    .map(Terminology::getSourceName)
                                    .collect(Collectors.toSet());
                            List<Terminology> terminologiesUse = new ArrayList<>();
                            String content = chapterExecute.getContent();
                            terminologyList.forEach(terminology -> {
                                if (content.contains(terminology.getSourceName())) {
                                    terminologiesUse.add(terminology);
                                }
                            });
                            Map<String, String> terminologyMap = terminologiesUse.stream()
                                    .collect(Collectors.toMap(
                                            Terminology::getSourceName,
                                            Terminology::getTargetName,
                                            (existingValue, newValue) -> newValue // 如果有重复键，用新值覆盖旧值
                                    ));
                            String terminologyJson = new Gson().toJson(terminologyMap);
                            String aiPromptReplace = aiPrompt.replace("<原术语表>", terminologyJson);
                            part = processMultilineString(part);
                            String translation = translation(platformName, apiUrl, part, model, true, aiPromptReplace, httpClient);
//                            System.out.println("====");
//                            System.out.println(translation);
//                            System.out.println("====");
                            ObjectMapper mapper = new ObjectMapper();
                            // 直接映射到 JsonData 对象
                            JsonData data;
                            try {
                                if (Objects.equals(translation, "无需汉化0ak2")) {
                                    data = new JsonData();
                                    data.setTable(new HashMap<>());
                                    data.setTranslation("");
                                } else {
                                    data = mapper.readValue(extractJson(translation), JsonData.class);
                                }
                            }
                            catch (JsonMappingException | JsonParseException e) {
                                Map<String, String> table = JsonEscapeUtils.getTable(translation);
                                String translation1 = JsonEscapeUtils.getTranslation(translation);
                                if (!translation1.isEmpty()) {
                                    data = new JsonData();
                                    data.setTable(table);
                                    data.setTranslation(translation1);
                                } else {
                                    throw new RuntimeException();
                                }
                            }
                            okContent.append(data.getTranslation()).append(System.lineSeparator());
                            terminologyMap.putAll(data.getTable());
                            List<String> list = terminologyList.stream().map(Terminology::getSourceName).toList();
                            Set<String> set = new HashSet<>(list);
                            if (!data.getTable().isEmpty()) {
                                Map<String, String> filteredTerminologyMap = data.getTable().entrySet().stream()
                                        .filter(entry -> !terminologyListSourceNames.contains(entry.getKey()))
                                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                                for (Map.Entry<String, String> table : filteredTerminologyMap.entrySet()) {
                                    if (set.contains(table.getKey())) continue;
                                    Terminology terminology = new Terminology(chapterExecute.getNovelId(), novel.getTrueId(), chapterExecute.getTrueId(), table.getKey(), table.getValue(), chapterExecute.getChapterNumber());
                                    terminologyService.save(terminology);
                                }
                            }
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
                            String obfuscator = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("TextObfuscator").getValueField();
                            Chapter isExist = chapterRepository.findByNovelIdAndChapterNumberAndIsDeletedFalse(chapterExecute.getNovelId(), chapterExecute.getChapterNumber());
                            if (isExist != null) {
                                int length = isExist.getContent().length();
                                isExist.setChapterNumber(chapterExecute.getChapterNumber());
                                String result = TextObfuscator.insertObfuscatedText(chapterExecute.getTranslatorContent(), obfuscator);
                                isExist.setContent(result);
                                isExist.setTitle(chapterExecute.getTitle());
                                isExist.setNovelId(chapterExecute.getNovelId());
                                isExist.setTrueId(chapterExecute.getTrueId());
                                isExist.setOwnPhoto(chapterExecute.isOwnPhoto());
                                novelRepository.incrementFontNumberById(isExist.getNovelId(), (long) (isExist.getContent().length() - length));
                                chapterRepository.save(isExist);
                                chapterSyncService.save(isExist);
                            } else {
                                Chapter chapter = new Chapter();
                                chapter.setChapterNumber(chapterExecute.getChapterNumber());
                                String result = TextObfuscator.insertObfuscatedText(chapterExecute.getTranslatorContent(), obfuscator);
                                chapter.setContent(result);
                                chapter.setTitle(chapterExecute.getTitle());
                                chapter.setNovelId(chapterExecute.getNovelId());
                                chapter.setTrueId(chapterExecute.getTrueId());
                                chapter.setOwnPhoto(chapterExecute.isOwnPhoto());
                                novelRepository.incrementFontNumberById(chapter.getNovelId(), (long) chapter.getContent().length());
                                chapterRepository.save(chapter);
                                chapterSyncService.save(chapter);
                                System.out.println("汉化完成：" + chapter.getId());
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
                    chapterExecuteRepository.save(chapterExecute);
                }
            }
        } finally {
            copyOnWriteArraySetRun.remove(novel.getId());
            uploadCopyOnWriteArraySetRun.remove(novel.getId());
        }
    }

    private void handleAbnormalTranslation(StringBuilder builder, String original) {
        builder.append("\n下方翻译出现异常！\n")
                .append(original)
                .append("\n上方翻译出现异常！\n");
    }





    // 定义结果封装类
    public static class ValidationResult {
        public List<String> parts;
        public List<Integer> abnormalIndices;

        public ValidationResult(List<String> parts, List<Integer> abnormalIndices) {
            this.parts = parts;
            this.abnormalIndices = abnormalIndices;
        }
    }

    public static ValidationResult panduanyichang(String content) {
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

        return new ValidationResult(parts, abnormalIndices);
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
                chunks.add(JsonEscapeUtils.escapeJsonString(currentChunk.toString().trim()));
                // 重置当前块并添加新行
                currentChunk.setLength(0);
                currentChunk.append(line).append("\n");
            }
        }

        // 处理最后一个块
        if (currentChunk.length() > 0) {
            chunks.add(JsonEscapeUtils.escapeJsonString(currentChunk.toString().trim()));
        }

        return chunks;
    }

    private static final Random RANDOM = new Random();

    public String translation(String platformName, String apiUrl, String content, String aiModel, boolean stream, String aiPrompt, OkHttpClient httpClient) throws Exception {
        if (content == null || content.trim().isEmpty()) {
            return "无需汉化0ak2";
        }
        Platform platform = platformRepository.findPlatformByPlatformName(platformName);
        apiUrl = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse(platformName).getValueField();
        List<PlatformApiKey> apiKeys = platformApiKeyRepository.findByPlatformIdAndIsDeletedFalse(platform.getId());
//        ThreadLocalRandom random = ThreadLocalRandom.current();
        String apiKey = apiKeys.get(RANDOM.nextInt(apiKeys.size())).getApiKey().replaceAll("\\r?\\n", "");
        if (content == null || content.isEmpty()) {
            return "本内容为空白内容，无需翻译";
        }
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
//                Thread.sleep(30000);
                throw new RuntimeException(apiKey + "Unexpected code " + response);
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
//                            System.out.println(text);
                            builder.append(text);
                        }
                    }
                }
            }
        }
        System.out.println("已翻译一部分" + apiKey + "_\n" + builder.toString());
        return builder.toString();
    }

    // 获取未完结小说
    public List<Novel> getNovels() {
        String up = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("upNumber").getValueField();
        // 读取并清空队列
        List<Novel> novelPia = novelRepository.findByPlatformEqualsAndUpGreaterThanAndIsDeletedFalseOrderByUpDesc("novelPia", Integer.parseInt(up));

        boolean valueField = Boolean.valueOf(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("shutuTr").getValueField());
        List<Novel> shutu = new ArrayList<>();
        if (valueField) {
            List<Novel> shutu1 = novelRepository.findByPlatformEqualsAndUpGreaterThanAndIsDeletedFalseOrderByUpDesc("shutu", Integer.parseInt(up));
            shutu.addAll(shutu1);
        }
        List<Novel> allNovel = new ArrayList<>();
        allNovel.addAll(novelPia);
        allNovel.addAll(shutu);
        return allNovel;
    }

    public List<Novel> getUploadNovels() {
        final boolean executeUploadTranslation = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeUploadTranslation").getValueField());
        if (executeUploadTranslation) {
            System.out.println("获取上传内容");
            return novelRepository.findByPlatformEqualsAndUpGreaterThanAndIsDeletedFalseOrderByUpDesc("upload", 0);
        } else {
            System.out.println("没有获取到上传内容");
            return new ArrayList<>();
        }
        // 读取并清空队列
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


    public String saveNovelFix(SyosetuNovelDetail syosetuNovelDetail, Novel save) {
        try {
            Long novelId = save.getId();
            List<String> tagList = syosetuNovelDetail.getTagList();
            for (String tag : tagList) {
                Tag tagToSave = new Tag(tag, "novelPia", tag);
                Optional<Tag> existingTag = tagRepository.findByTrueName(tagToSave.getTrueName());
                if (existingTag.isEmpty()) {
                    Tag syosetuTag = tagRepository.save(tagToSave);
                    novelTagRepository.save(new NovelTag(novelId, syosetuTag.getId()));
                } else {
                    Tag tag1 = existingTag.get();
                    novelTagRepository.save(new NovelTag(novelId, tag1.getId()));
                }
            }
            return "已收录本小说，请耐心等待<定时汉化任务>执行，这可能需要几个小时的时间：{"+ novelId +"}";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String saveNovel(String novelTrueId, SyosetuNovelDetail syosetuNovelDetail, Novel save) {
        try {
            executeId.add(novelTrueId);
            final String epFormat = "EP%s";
            Long novelId = save.getId();
            List<String> tagList = syosetuNovelDetail.getTagList();
            for (String tag : tagList) {
                Tag tagToSave = new Tag(tag, "novelPia", tag);
                Optional<Tag> existingTag = tagRepository.findByPlatformAndTrueName("novelPia",tagToSave.getTrueName());
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
            ChapterExecute save1 = chapterExecuteRepository.save(chapter);
            List<Novel> novels = new ArrayList<>();
            novels.add(save);
            executeDownloadOneNovel(novels);
            return "已收录本小说，请耐心等待<定时汉化任务>执行，这可能需要几个小时的时间：{"+ novelId +"}";
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executeId.remove(novelTrueId);
        }
    }


    // 搜索功能
    public String searchWeb(String keyword) {
        String encode = URLEncoder.encode(keyword);
        final boolean proxyClient = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("proxyClient").getValueField());
        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
        try(CloseableHttpClient httpClient = proxyClient ? createHttpClientProxy() : createHttpClient()) {
            String novelPiaSearchUrl = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaSearchUrl").getValueField();
//            String cookie = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaCookie").getValueField();
            final String cookie = dictionaryService.getOneCookie();
            if (cookie == null || cookie.isEmpty()) {
                throw new RuntimeException("当前无可用的cookie");
            }
            return sendGetRequest(httpClient, String.format(novelPiaSearchUrl, encode), cookie);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SyosetuNovelDetail saveNovelDetail(String novelTrueId) throws Exception {
        StringBuilder prologue = new StringBuilder();
        List<String> tagList = new ArrayList<>();
        String title = "";
        String novelType = "";
        String novelPiaDetail = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaDetail").getValueField();
//        String cookie = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaCookie").getValueField();
        final String cookie = dictionaryService.getOneCookie();
        if (cookie == null || cookie.isEmpty()) {
            throw new RuntimeException("当前无可用的cookie");
        }
        // 使用Jsoup发送GET请求并解析HTML
        final boolean proxyClient = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("proxyClient").getValueField());
        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);


        Connection connection = Jsoup.connect(String.format(novelPiaDetail, novelTrueId));

        connection.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .timeout(10000)
                .header("Cookie", cookie);

        if (proxyClient) {
            Proxy socksProxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
            connection.proxy(socksProxy);
        }

        Document document = connection.get();

        // 获取class=epnew-novel-info的div标签
        Element novelInfoDiv = document.selectFirst("div.epnew-novel-info");

        if (novelInfoDiv != null) {
            // 获取标题
            Element titleElement = novelInfoDiv.selectFirst("div.epnew-novel-title");
            title = titleElement.text();

            // 获取标签列表
            Elements tagElements = novelInfoDiv.select("div.epnew-tag p.writer-tag span.tag");
            for (Element tagElement : tagElements) {
                String tag = tagElement.text().replace("#", "");
                tagList.add(tag);
            }

            // 获取简介
            Element synopsisElement = novelInfoDiv.selectFirst("div.synopsis");
            String synopsis = synopsisElement.html();
            // 将<br>标签替换为换行符
            String textWithNewlines = synopsis.replaceAll("<br\\s*/?>", "\n");
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
        return new SyosetuNovelDetail(prologue.toString(),title,novelTrueId,tagList,novelType);
    }

    public List<ChapterExecute> executeDownloadOneNovel(List<Novel> novels) {
        // 从数据库获取配置参数（这些配置在方法执行期间不变）
        final String regex = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaRegex").getValueField();
        final Pattern pattern = Pattern.compile(regex);
        final String unreleased = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaUnreleased").getValueField();
        final String unreleasedKeyWord = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaUnreleasedKeyWord").getValueField();
        final String getNovelChapter = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaGetNovelChapter").getValueField();
        final String epFormat = "EP%s";
        final String getPage = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaGetPage").getValueField();
//        final String cookie = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaCookie").getValueField();
        final String cookie = dictionaryService.getOneCookie();
        if (cookie == null || cookie.isEmpty()) {
            throw new RuntimeException("当前无可用的cookie");
        }
        final String novelPiaDetail = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaDetail").getValueField();
        List<ChapterExecute> chapterExecuteList = Collections.synchronizedList(new ArrayList<>());

        // 创建线程池（根据实际情况调整核心线程数）
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService executor = Executors.newFixedThreadPool(corePoolSize);
        final boolean proxyClient = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("proxyClient").getValueField());
        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
        try(CloseableHttpClient httpClient = proxyClient ? createHttpClientProxy() : createHttpClient()) {
            List<CompletableFuture<Void>> futures = novels.stream()
                    .map(novel -> CompletableFuture.runAsync(() -> {
                        String s = null;
                        try {
                            s = sendGetRequest(httpClient, String.format(novelPiaDetail, novel.getTrueId()), cookie);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        if (s.contains("삭제된 소설 입니다")) {
                            // 本小说已被删除
                            return;
                        }

                        try {
                            String like = extractLikeText(s).replaceAll(",", "");
                            String read = extractReadText(s).replaceAll(",", "");
                            Map.Entry<String, String> stringStringEntry = extractWriterText(s);
                            novel.setAuthorId(stringStringEntry.getKey());
                            novel.setAuthorName(stringStringEntry.getValue());
                            novel.setNovelLike(like.isEmpty()?0L : Long.parseLong(like));
                            novel.setNovelRead(read.isEmpty()?0L : Long.parseLong(read));
                            List<String> spans = extractAllBadgeTexts(s);
                            List<String> strings = translateSpans(spans);
                            novel.setSpans(String.join(" ", strings));
                            novelRepository.save(novel);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        try {
                            // 获取当前小说的章节信息
                            List<String> existingChapters = chapterRepository
                                    .findTitlesByNovelIdAndIsDeletedFalse(novel.getId());
                            List<String> executingChapters = chapterExecuteRepository
                                    .findTitlesByNovelIdAndIsDeletedFalse(novel.getId());
                            Set<String> existingChaptersSet = new HashSet<>(existingChapters);
                            existingChaptersSet.addAll(executingChapters);
                            // 获取章节映射关系
                            Map<String, Boolean> episodeMap = buildFormDataFromMap(
                                    httpClient, cookie, getPage, novel.getTrueId()
                            );

                            int episodeCounter = 1;
                            for (Map.Entry<String, Boolean> entry : episodeMap.entrySet()) {
                                String episodeId = entry.getKey();
                                Boolean photo = entry.getValue();
                                String episodeFile = String.format(epFormat, String.format("%04d", episodeCounter));

                                // 检查章节是否需要处理
                                if (!existingChaptersSet.contains(episodeFile)) {

                                    try {
                                        // 获取章节内容
                                        String jsonResponse = sendGetRequest(
                                                httpClient,
                                                String.format(getNovelChapter, episodeId),
                                                cookie
                                        );
                                        String content = extractContent(jsonResponse, pattern,episodeId);

                                        // 处理未发布章节
                                        if (content.isEmpty()) {
                                            String unreleasedResponse = sendGetRequest(
                                                    httpClient,
                                                    String.format(unreleased, episodeId),
                                                    cookie
                                            );
                                            if (unreleasedResponse.contains(unreleasedKeyWord)) {
                                                continue;
                                            }
                                        }

                                        content = content.replaceAll("V1Zn[A-Za-z0-9+=]+", "");
                                        // 保存章节执行记录
                                        ChapterExecute chapter = new ChapterExecute(
                                                novel.getId(),
                                                episodeFile,
                                                episodeCounter,
                                                content,
                                                0,
                                                episodeId,
                                                photo
                                        );
                                        ChapterExecute saved = chapterExecuteRepository.save(chapter);

                                        // 在这里获取图片位置以及链接

                                        // 添加同步锁保证控制台输出有序性
                                        synchronized (System.out) {
                                            System.out.println("已保存：" + novel.getTitle() + "--" + saved.getTitle());
                                        }

                                        chapterExecuteList.add(saved);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                episodeCounter += 1;
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


    // 定时任务
//    @Scheduled(cron = "0 5,35,55 * * * ?")
    public void fixErrorChapter() {
        if (executeError.compareAndSet(true, false)) {
            try {
                final boolean executeNovelPiaDownloadError = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeNovelPiaDownloadError").getValueField());
                final boolean executeNovelPiaTrError = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeNovelPiaTrError").getValueField());

                if (!executeNovelPiaDownloadError) {
                    return;
                }
                List<UserFeedback> byIsDeleteFalse = userFeedbackRepository.findByIsDeleteFalse();
                // 提取 novel_id 列表
                List<Long> novelIds = byIsDeleteFalse.stream()
                        .map(UserFeedback::getNovelId)
                        .toList();
                List<Novel> novelPia = novelRepository.findByIdInAndPlatformEqualsAndIsDeletedFalse(novelIds, "novelPia");
                // 提取 novelPia 中的 id 列表
                Set<Long> novelPiaIds = novelPia.stream()
                        .filter(novel -> !Objects.equals(novel.getNovelType(), "删除"))
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
                if (chapterList.isEmpty()) {
                    return;
                }
                // 最新下载的
                List<ChapterErrorExecute> chapterErrorExecutes = executeDownloadErrorChapter(chapterList);
                List<ChapterErrorExecute> noExecuted = chapterErrorExecutes.stream().filter(chapterErrorExecute -> chapterErrorExecute.getNowState() == 0).toList();
                List<ChapterErrorExecute> executed = chapterErrorExecutes.stream().filter(chapterErrorExecute -> chapterErrorExecute.getNowState() == 1).toList();
                if (!executeNovelPiaTrError) {
                    return;
                }
                String platformName = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("enableTrModel").getValueField();
                List<ChapterErrorExecute> chapterErrorExecutesExceptions = executeTranslationError(platformName, noExecuted);
                chapterErrorExecutesExceptions.addAll(executed);
                executeTranslationExceptionError(platformName, executed);
            } finally {
                executeError.set(true);
            }
        }
    }


    public List<ChapterErrorExecute> executeDownloadErrorChapter(List<Chapter> chapterList) {
        // 从数据库获取配置参数（这些配置在方法执行期间不变）
        final String regex = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaRegex").getValueField();
        final Pattern pattern = Pattern.compile(regex);
        final String getNovelChapter = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaGetNovelChapter").getValueField();
//        final String cookie = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("novelPiaCookie").getValueField();
        final String cookie = dictionaryService.getOneCookie();
        if (cookie == null || cookie.isEmpty()) {
            throw new RuntimeException("当前无可用的cookie");
        }
        List<ChapterErrorExecute> chapterExecuteList = Collections.synchronizedList(new ArrayList<>());

        // 创建线程池（根据实际情况调整核心线程数）
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(chapterList.size(), corePoolSize));
        final boolean proxyClient = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("proxyClient").getValueField());
        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
//        System.out.println("proxyClient::" + proxyClient);
        try(CloseableHttpClient httpClient = proxyClient ? createHttpClientProxy() : createHttpClient()) {
            List<CompletableFuture<Void>> futures = chapterList.stream()
                    .map(chapter -> CompletableFuture.runAsync(() -> {
                        try {
                            String episodeId = chapter.getTrueId();
                            ChapterErrorExecute byNovelIdAndChapterNumberAndIsDeletedFalse = chapterErrorExecuteRepository.findByNovelIdAndChapterNumber(chapter.getNovelId(), chapter.getChapterNumber());
                            if (byNovelIdAndChapterNumberAndIsDeletedFalse != null && !byNovelIdAndChapterNumberAndIsDeletedFalse.isDeleted()) {
                                chapterExecuteList.add(byNovelIdAndChapterNumberAndIsDeletedFalse);
                                return;
                            }
                            try {
                                // 获取章节内容
                                String jsonResponse = sendGetRequest(
                                        httpClient,
                                        String.format(getNovelChapter, episodeId),
                                        cookie
                                );
                                String content = extractContentError(jsonResponse, pattern);

                                // 处理未发布章节
                                if (content.isEmpty() || content.contains("获取章节失败")) {
                                    return;
                                }

                                content = content.replaceAll("V1Zn[A-Za-z0-9+=]+", "");

                                if (byNovelIdAndChapterNumberAndIsDeletedFalse != null && byNovelIdAndChapterNumberAndIsDeletedFalse.isDeleted()) {
                                    byNovelIdAndChapterNumberAndIsDeletedFalse.setDeleted(false);
                                    byNovelIdAndChapterNumberAndIsDeletedFalse.setContent(content);
                                    byNovelIdAndChapterNumberAndIsDeletedFalse.setTranslatorContent("");
                                    byNovelIdAndChapterNumberAndIsDeletedFalse.setNowState(0);
                                    ChapterErrorExecute save = chapterErrorExecuteRepository.save(byNovelIdAndChapterNumberAndIsDeletedFalse);
                                    chapterExecuteList.add(save);
                                    return;
                                }
                                // 保存章节执行记录
                                ChapterErrorExecute chapterErrorExecute = new ChapterErrorExecute(
                                        chapter.getNovelId(),
                                        chapter.getTitle(),
                                        chapter.getChapterNumber(),
                                        content,
                                        0,
                                        episodeId,
                                        chapter.isOwnPhoto()
                                );
                                ChapterErrorExecute saved = chapterErrorExecuteRepository.save(chapterErrorExecute);
                                chapterExecuteList.add(saved);
                            } catch (Exception e) {
                                e.printStackTrace();
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


    public void executeTranslationExceptionError(String platformName, List<ChapterErrorExecute> chapterExecuteList) {
        // 预加载公共配置（线程安全方式）
        String apiUrl = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse(platformName).getValueField();
        String model = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("siliconflowModel_1").getValueField();
        String aiPrompt = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("aiPrompt").getValueField();
        Platform platform = platformRepository.findPlatformByPlatformName(platformName);
        List<PlatformApiKey> apiKeys = platformApiKeyRepository.findByPlatformIdAndIsDeletedFalse(platform.getId());
//        OkHttpClient httpClient = createOKHttpClient();
        final boolean AIproxyClient = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("AIproxyClient").getValueField());
        OkHttpClient httpClient = AIproxyClient ? createAIHttpClientProxy(proxyHost, proxyPort) : createOKHttpClient();
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        try {
            Map<Long, List<ChapterErrorExecute>> groupedByNovelId = chapterExecuteList.stream()
                    .collect(Collectors.groupingBy(
                            ChapterErrorExecute::getNovelId,
                            Collectors.mapping(
                                    ce -> ce,
                                    Collectors.toList()
                            )
                    ))
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().stream()
                                    .sorted(Comparator.comparingInt(ChapterErrorExecute::getChapterNumber))
                                    .collect(Collectors.toList())
                    ));
            // 并行处理每个章节
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (Map.Entry<Long, List<ChapterErrorExecute>> entry : groupedByNovelId.entrySet()) {
                List<ChapterErrorExecute> chapters = entry.getValue();
                futures.add(CompletableFuture.runAsync(() ->
                                processSingleChapterError(chapters, platformName, apiUrl, model, apiKeys, aiPrompt, httpClient),
                        executor));
            }
//
//            List<CompletableFuture<Void>> futures = chapterExecuteList.stream()
//                    .filter(chapter -> chapter.getNowState() != 3)
//                    .map(chapter -> CompletableFuture.runAsync(() -> {
//                        try {
//                            // 处理单个章节
//                            ChapterErrorExecute executed = processSingleChapterError(chapter, platformName, apiUrl, model, apiKeys, aiPrompt, httpClient);
//
//                            // 保存最终章节
//                            if (executed.getNowState() == 2) {
//                                saveFinalChapterError(executed);
//                            }
//                        } catch (Exception e) {
//                            handleChapterError(chapter, e);
//                        }
//                    }, executor))
//                    .toList();
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
        String obfuscator = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("TextObfuscator").getValueField();
        if (chapter != null) {
            int length = chapter.getContent().length();
            String result = TextObfuscator.insertObfuscatedText(executed.getTranslatorContent(), obfuscator);
            chapter.setContent(result);
            novelRepository.incrementFontNumberById(chapter.getNovelId(), (long) (chapter.getContent().length() - length));
            chapterRepository.save(chapter);
            chapterSyncService.save(chapter);
            userFeedbackRepository.softDeleteByUserAndContent(executed.getNovelId(), chapter.getId());
            chapterErrorExecuteRepository.softDeleteById(executed.getId());
        } else {
            // 只要程序没问题，不可能进入这个逻辑
            Chapter chapter1 = new Chapter();
            chapter1.setChapterNumber(executed.getChapterNumber());
            String result = TextObfuscator.insertObfuscatedText(executed.getTranslatorContent(), obfuscator);
            chapter1.setContent(result);
            chapter1.setTitle(executed.getTitle());
            chapter1.setNovelId(executed.getNovelId());
            chapter1.setOwnPhoto(executed.isOwnPhoto());
            chapter1.setTrueId(executed.getTrueId());
            novelRepository.incrementFontNumberById(chapter1.getNovelId(), (long) chapter1.getContent().length());
            chapterRepository.save(chapter1);
            chapterSyncService.save(chapter1);
        }
    }

    private void processSingleChapterError(List<ChapterErrorExecute> chapters, String platformName,
                                                String apiUrl, String model, List<PlatformApiKey> apiKeys, String aiPrompt, OkHttpClient httpClient) {
        if (chapters.isEmpty()) {
            return;
        }
        Novel novel = novelRepository.findByIdAndIsDeletedFalse(chapters.get(0).getNovelId());
        for (ChapterErrorExecute chapter : chapters) {
            chapter.setContent(ShuTuNovelChapterService.processText(chapter.getContent()));
            try {
                // 使用线程安全的随机数
                ThreadLocalRandom random = ThreadLocalRandom.current();
                chapter.setTranslatorContent(chapter.getTranslatorContent().replaceAll("V1Zn[A-Za-z0-9+=]+", ""));

                // 状态检查和初始化
                if (chapter.getNowState() == 3) continue;
                ValidationResult result = panduanyichang(chapter.getTranslatorContent());
                try {
                    // 更新状态需要同步
                    synchronized (chapter) {
                        chapter.setNowState(3);
                        chapterErrorExecuteRepository.save(chapter);
                    }

                    // 处理异常内容
                    for (Integer index : result.abnormalIndices) {
                        List<Terminology> terminologyList = terminologyService.findAllByNovelId(chapters.get(0).getNovelId());
                        Set<String> terminologyListSourceNames = terminologyList.stream()
                                .map(Terminology::getSourceName)
                                .collect(Collectors.toSet());
                        List<Terminology> terminologiesUse = new ArrayList<>();
                        String content = chapter.getTranslatorContent();
                        terminologyList.forEach(terminology -> {
                            if (content.contains(terminology.getSourceName())) {
                                terminologiesUse.add(terminology);
                            }
                        });
                        Map<String, String> terminologyMap = terminologiesUse.stream()
                                .collect(Collectors.toMap(
                                        Terminology::getSourceName,
                                        Terminology::getTargetName,
                                        (existingValue, newValue) -> newValue
                                ));
                        String terminologyJson = new Gson().toJson(terminologyMap);
                        String aiPromptReplace = aiPrompt.replace("<原术语表>", terminologyJson);
                        processAbnormalSegmentError(novel,terminologyMap, terminologyList,terminologyListSourceNames, result, index, apiKeys.get(random.nextInt(apiKeys.size())), apiUrl, model, chapter, aiPromptReplace, httpClient,platformName);
                    }
                }finally {
                    // 更新最终状态
                    synchronized (chapter) {
                        chapter.setTranslatorContent(String.join("\n", result.parts));
                        chapter.setNowState(chapter.getNowState() == 3 ? 2 : 1);
                        chapterErrorExecuteRepository.save(chapter);
                    }
                }
                // 保存最终章节
                if (chapter.getNowState() == 2) {
                    saveFinalChapterError(chapter);
                }
            } catch (Exception e) {
                handleChapterError(chapter, e);
            }
        }
    }

    private String processAbnormalSegmentError(Novel novel,Map<String, String> terminologyMap,List<Terminology> terminologyList,Set<String> terminologyListSourceNames,ValidationResult result, int index,
                                        PlatformApiKey apiKey, String apiUrl,
                                        String model, ChapterErrorExecute chapter, String aiPrompt, OkHttpClient httpClient,String platformName) {
        String original = result.parts.get(index);
        StringBuilder content = new StringBuilder();

        try {
            String translation = translation(platformName, apiUrl, original, model, true, aiPrompt, httpClient);
            String s = content.append(translation).toString();
            ObjectMapper mapper = new ObjectMapper();
            // 直接映射到 JsonData 对象
            JsonData data;
            try {
                if (Objects.equals(translation, "无需汉化0ak2")) {
                    data = new JsonData();
                    data.setTable(new HashMap<>());
                    data.setTranslation("");
                } else {
                    data = mapper.readValue(extractJson(s), JsonData.class);
                }
            } catch (JsonMappingException e) {
                Map<String, String> table = JsonEscapeUtils.getTable(translation);
                String translation1 = JsonEscapeUtils.getTranslation(translation);
                if (!translation1.isEmpty()) {
                    data = new JsonData();
                    data.setTable(table);
                    data.setTranslation(translation1);
                } else {
                    throw new RuntimeException();
                }
            }

            terminologyMap.putAll(data.getTable());
            List<String> list = terminologyList.stream().map(Terminology::getSourceName).toList();
            Set<String> set = new HashSet<>(list);

            if (!data.getTable().isEmpty()) {
                Map<String, String> filteredTerminologyMap = data.getTable().entrySet().stream()
                        .filter(entry -> !terminologyListSourceNames.contains(entry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                for (Map.Entry<String, String> table : filteredTerminologyMap.entrySet()) {
                    if (set.contains(table.getKey())) continue;
                    Terminology terminology = new Terminology(chapter.getNovelId(), novel.getTrueId(), chapter.getTrueId(), table.getKey(), table.getValue(), chapter.getChapterNumber());
                    terminologyService.save(terminology);
                }
            }
            result.parts.set(index, data.getTranslation());
        } catch (Exception e) {
            result.parts.set(index, buildAbnormalContent(original));
            chapter.setNowState(1);
            e.printStackTrace();
        }

        return content.toString();
    }


}
