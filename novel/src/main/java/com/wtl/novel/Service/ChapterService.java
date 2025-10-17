package com.wtl.novel.Service;

import com.wtl.novel.CDO.ChapterCDO;
import com.wtl.novel.CDO.ChapterSyncCTO;
import com.wtl.novel.CDO.TerminologyModifyCTO;
import com.wtl.novel.DTO.ChapterProjection;
import com.wtl.novel.DTO.SourceTargetProjection;
import com.wtl.novel.DTO.TextNumCount;
import com.wtl.novel.DTO.UserIdUsernameDTO;
import com.wtl.novel.entity.*;
import com.wtl.novel.repository.*;
import com.wtl.novel.scalingUp.entity.ChapterScalingUpOne;
import com.wtl.novel.scalingUp.repository.ChapterScalingUpOneRepository;
import com.wtl.novel.util.ObfuscateFontOTF;
import com.wtl.novel.util.QuoteModifier;
import com.wtl.novel.util.StringEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ChapterService {
    private static final Logger log = LoggerFactory.getLogger(ChapterService.class);

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private UserGlossaryRepository userGlossaryRepository;
    @Autowired
    private ChapterCommentService chapterCommentService;
    @Autowired
    private UserChapterEditRepository userChapterEditRepository;
    @Autowired
    private ChapterCopyRepository chapterCopyRepository;
    @Autowired
    private NovelRepository novelRepository;
    @Autowired
    private ReadingRecordService readingRecordService;
    @Autowired
    private ObfuscateFontOTF obfuscateFontOTF;
    @Autowired
    private ChapterImageLinkService chapterImageLinkService;
    @Autowired
    private  ChapterSyncRepository chapterSyncRepository;
    @Autowired(required = false)
    private ChapterScalingUpOneRepository chapterScalingUpOneRepository;
    private final static QuoteModifier quoteModifier = new QuoteModifier();

    public void delete(Long chapterId) {
        chapterRepository.deleteById(chapterId);
    }

    public static void applyFontMapFast(ChapterCDO chapterCDO, Map<String, String> fontMap) {
        if (chapterCDO == null || chapterCDO.getContent() == null || fontMap == null || fontMap.isEmpty()) {
            return;
        }

        String content = chapterCDO.getContent();

        // 构建正则：所有 key 用 | 分隔
        StringBuilder regex = new StringBuilder();
        for (String key : fontMap.keySet()) {
            regex.append(Pattern.quote(key)).append("|");
        }
        regex.setLength(regex.length() - 1); // 去掉最后一个 |

        Pattern pattern = Pattern.compile(regex.toString());

        // 使用 Matcher 替换
        String result = pattern.matcher(content).replaceAll(matchResult -> {
            String m = matchResult.group();
            return fontMap.getOrDefault(m, m);
        });

        chapterCDO.setContent(result);
    }

    public static String applyFontMapFast(String content, Map<String, String> fontMap) {
        if (content == null || fontMap == null || fontMap.isEmpty()) {
            return "";
        }
        // 构建正则：所有 key 用 | 分隔
        StringBuilder regex = new StringBuilder();
        for (String key : fontMap.keySet()) {
            regex.append(Pattern.quote(key)).append("|");
        }
        regex.setLength(regex.length() - 1); // 去掉最后一个 |

        Pattern pattern = Pattern.compile(regex.toString());

        // 使用 Matcher 替换

        return pattern.matcher(content).replaceAll(matchResult -> {
            String m = matchResult.group();
            return fontMap.getOrDefault(m, m);
        });
    }



    public ChapterCDO getChapterByVersion(Long id, Long versionId,Long userId) {
        Chapter byIdAndIsDeletedFalse = chapterRepository.findByIdAndIsDeletedFalse(id);
        ChapterCDO chapterCDO;
        if (versionId == 0) {
            chapterCDO = findByIdAndIsDeletedFalse(id);
        } else {
            Optional<UserChapterEdit> byUserIdAndChapterId = userChapterEditRepository.findByUserIdAndChapterId(versionId, id);
            if (byUserIdAndChapterId.isPresent()) {
                chapterCDO = new ChapterCDO(byIdAndIsDeletedFalse);
                chapterCDO.setContent(byUserIdAndChapterId.get().getContent());
            } else {
                byIdAndIsDeletedFalse.setContent(StringEncoder.cleanText(byIdAndIsDeletedFalse.getContent()));
                chapterCDO = new ChapterCDO(byIdAndIsDeletedFalse);
            }
        }
        List<SourceTargetProjection> termList = userGlossaryRepository.findByNovelId(chapterCDO.getNovelId());
        Map<String, String> map = termList.stream()
                .collect(Collectors.toMap(SourceTargetProjection::getSourceName,
                        SourceTargetProjection::getTargetName,
                        (a, b) -> a));   // 如有重复 key 保留前者

        String newContent1 = map.keySet()
                .stream()
                .sorted((k1, k2) -> Integer.compare(k2.length(), k1.length())) // 长词在前
                .reduce(chapterCDO.getContent(),
                        (txt, src) -> txt.replace(src, map.get(src)));

        chapterCDO.setContent(newContent1);
        applyFontMapFast(chapterCDO, obfuscateFontOTF.fontMap);



        List<TextNumCount> textNumCounts = chapterCommentService.countByChapterIdGroupByTextNum(id);
        Integer totalLines = chapterCDO.getContent().split("\n").length;
        List<Integer> max = textNumCounts.stream()
                .map(TextNumCount::getTextNum)
                .filter(textNum -> textNum > totalLines)
                .toList();
        chapterCommentService.moveComments(id,max,totalLines);
        textNumCounts = chapterCommentService.countByChapterIdGroupByTextNum(id);
        chapterCDO.setTextNumCounts(textNumCounts);


//        String newContent1 = chapterCDO.getContent();
//        String newContent1 = StringEncoder.insertEncodedTextRandomly(chapterCDO.getContent(),userId.toString());
//        newContent1 = TextObfuscator.embedDigitsInText(userId, newContent1);
//        chapterCDO.setContent(quoteModifier.parse(newContent1, userId));
        if (byIdAndIsDeletedFalse.isOwnPhoto()) {
            List<ChapterImageLink> chapterImageLinkList = chapterImageLinkService.findByChapterTrueId(byIdAndIsDeletedFalse.getTrueId());

            Set<String> existingContentLinkLocationPairs = new HashSet<>();
            List<ChapterImageLink> duplicatesToDelete = new ArrayList<>();
            List<ChapterImageLink> uniqueChapterImageLinkList = new ArrayList<>();

            for (ChapterImageLink current : chapterImageLinkList) {
                String contentLinkLocationPair = current.getContentLink() + "-" + current.getLocation();

                // 如果这对组合已经存在于集合中，说明是重复数据，需要删除
                if (existingContentLinkLocationPairs.contains(contentLinkLocationPair)) {
                    duplicatesToDelete.add(current);
                } else {
                    // 如果这对组合不存在于集合中，添加到集合中，并将当前对象添加到保留列表中
                    existingContentLinkLocationPairs.add(contentLinkLocationPair);
                    uniqueChapterImageLinkList.add(current);
                }
            }
            if (!duplicatesToDelete.isEmpty()) {
                chapterImageLinkService.deleteAll(duplicatesToDelete);
            }


            String newContent = insertContentLinks(chapterCDO.getContent(), uniqueChapterImageLinkList);
            chapterCDO.setContent(newContent);
        }
        Long novelId = byIdAndIsDeletedFalse.getNovelId();
        int chapterNumber = byIdAndIsDeletedFalse.getChapterNumber();

        if (chapterNumber > 1) {
            Long preChapterID = chapterRepository.findIdByNovelIdAndChapterNumberAndIsDeletedFalse(novelId, chapterNumber - 1);
            chapterCDO.setPreId(preChapterID);
        }
        Long nextChapterID = chapterRepository.findIdByNovelIdAndChapterNumberAndIsDeletedFalse(novelId, chapterNumber + 1);
        chapterCDO.setNextId(nextChapterID);
        if (userId < 0) {
            return chapterCDO;
        }
        readingRecordService.updateReadingRecord(userId, byIdAndIsDeletedFalse.getNovelId(), (long) byIdAndIsDeletedFalse.getChapterNumber(),byIdAndIsDeletedFalse.getId());
        return chapterCDO;
    }

    // 主机标识常量
    private static final String HOST_SERVER_NAME = "host1";
    
    public ChapterCDO findByIdAndIsDeletedFalse(Long id) {
        Optional<ChapterSync> chapterSync = chapterSyncRepository.findByChapterId(id);
        Chapter chapter = chapterRepository.findByIdAndIsDeletedFalse(id);
        
        if (chapter == null) {
            log.error("章节不存在，chapterId={}", id);
            throw new IllegalArgumentException("章节不存在: " + id);
        }
        
        // 情况1: 未配置扩展数据库 -> 从主库读取
        if (chapterScalingUpOneRepository == null) {
            chapter.setContent(StringEncoder.cleanText(chapter.getContent()));
            return new ChapterCDO(chapter);
        }
        
        // 情况2: 章节未同步 -> 从主库读取
        if (chapterSync.isEmpty() || !Boolean.TRUE.equals(chapterSync.get().getSynced())) {
            chapter.setContent(StringEncoder.cleanText(chapter.getContent()));
            return new ChapterCDO(chapter);
        }
        
        // 情况3: 检查主机标识
        ChapterSync sync = chapterSync.get();
        if (!HOST_SERVER_NAME.equals(sync.getHostServerName())) {
            log.debug("主机标识不匹配，降级到主库读取，chapterId={}, expected={}, actual={}", 
                     id, HOST_SERVER_NAME, sync.getHostServerName());
            chapter.setContent(StringEncoder.cleanText(chapter.getContent()));
            return new ChapterCDO(chapter);
        }
        
        // 情况4: 从扩展数据库读取（带类型安全检查）
        try {
            Optional<ChapterScalingUpOne> scalingUpOpt = 
                chapterScalingUpOneRepository.findById(chapter.getId());
            
            if (scalingUpOpt.isPresent()) {
                ChapterScalingUpOne scalingUpChapter = scalingUpOpt.get();
                // 确保返回的是正确类型
                if (scalingUpChapter.getId() == null || !scalingUpChapter.getId().equals(id)) {
                    log.error("从库数据ID不匹配，expected={}, actual={}", id, scalingUpChapter.getId());
                    throw new IllegalStateException("从库数据ID不匹配");
                }
                return new ChapterCDO(scalingUpChapter);
            } else {
                // 扩展库中数据不存在，降级到主库
                log.warn("章节已标记为同步但从库中不存在，降级到主库读取，chapterId={}", id);
                chapter.setContent(StringEncoder.cleanText(chapter.getContent()));
                return new ChapterCDO(chapter);
            }
        } catch (Exception e) {
            // 从库查询异常，降级到主库
            log.error("从库查询异常，降级到主库读取，chapterId={}", id, e);
            chapter.setContent(StringEncoder.cleanText(chapter.getContent()));
            return new ChapterCDO(chapter);
        }
    }

    public ChapterCDO findChapterById(Long id,Long userId) {
//        Chapter byIdAndIsDeletedFalse = chapterRepository.findByIdAndIsDeletedFalse(id);
//        byIdAndIsDeletedFalse.setContent(StringEncoder.cleanText(byIdAndIsDeletedFalse.getContent()));
//        ChapterCDO chapterCDO = new ChapterCDO(byIdAndIsDeletedFalse);
        ChapterCDO chapterCDO = findByIdAndIsDeletedFalse(id);
        List<SourceTargetProjection> termList = userGlossaryRepository.findByNovelId(chapterCDO.getNovelId());
        Map<String, String> map = termList.stream()
                .collect(Collectors.toMap(SourceTargetProjection::getSourceName,
                        SourceTargetProjection::getTargetName,
                        (a, b) -> a));   // 如有重复 key 保留前者
        String newContent1 = map.keySet()
                .stream()
                .sorted((k1, k2) -> Integer.compare(k2.length(), k1.length())) // 长词在前
                .reduce(chapterCDO.getContent(),
                        (txt, src) -> txt.replace(src, map.get(src)));


        chapterCDO.setContent(newContent1);
        applyFontMapFast(chapterCDO, obfuscateFontOTF.fontMap);


        List<TextNumCount> textNumCounts = chapterCommentService.countByChapterIdGroupByTextNum(id);
        Integer totalLines = chapterCDO.getContent().split("\n").length;
        List<Integer> max = textNumCounts.stream()
                .map(TextNumCount::getTextNum)
                .filter(textNum -> textNum > totalLines)
                .toList();
        chapterCommentService.moveComments(id,max,totalLines);
        textNumCounts = chapterCommentService.countByChapterIdGroupByTextNum(id);
        chapterCDO.setTextNumCounts(textNumCounts);

//        String newContent1 = chapterCDO.getContent();
//        String newContent1 = StringEncoder.insertEncodedTextRandomly(chapterCDO.getContent(),userId.toString());
//        newContent1 = TextObfuscator.embedDigitsInText(userId, newContent1);
//        chapterCDO.setContent(quoteModifier.parse(newContent1, userId));
        if (chapterCDO.isOwnPhoto()) {
            List<ChapterImageLink> chapterImageLinkList = chapterImageLinkService.findByChapterTrueId(chapterCDO.getTrueId());

            Set<String> existingContentLinkLocationPairs = new HashSet<>();
            List<ChapterImageLink> duplicatesToDelete = new ArrayList<>();
            List<ChapterImageLink> uniqueChapterImageLinkList = new ArrayList<>();

            for (ChapterImageLink current : chapterImageLinkList) {
                String contentLinkLocationPair = current.getContentLink() + "-" + current.getLocation();

                // 如果这对组合已经存在于集合中，说明是重复数据，需要删除
                if (existingContentLinkLocationPairs.contains(contentLinkLocationPair)) {
                    duplicatesToDelete.add(current);
                } else {
                    // 如果这对组合不存在于集合中，添加到集合中，并将当前对象添加到保留列表中
                    existingContentLinkLocationPairs.add(contentLinkLocationPair);
                    uniqueChapterImageLinkList.add(current);
                }
            }
            if (!duplicatesToDelete.isEmpty()) {
                chapterImageLinkService.deleteAll(duplicatesToDelete);
            }


            String newContent = insertContentLinks(chapterCDO.getContent(), uniqueChapterImageLinkList);
            chapterCDO.setContent(newContent);
        }
        Long novelId = chapterCDO.getNovelId();
        int chapterNumber = chapterCDO.getChapterNumber();

        if (chapterNumber > 1) {
            Long preChapterID = chapterRepository.findIdByNovelIdAndChapterNumberAndIsDeletedFalse(novelId, chapterNumber - 1);
            chapterCDO.setPreId(preChapterID);
        }
        Long nextChapterID = chapterRepository.findIdByNovelIdAndChapterNumberAndIsDeletedFalse(novelId, chapterNumber + 1);
        chapterCDO.setNextId(nextChapterID);
        if (userId < 0) {
            return chapterCDO;
        }
        readingRecordService.updateReadingRecord(userId, chapterCDO.getNovelId(), (long) chapterCDO.getChapterNumber(),chapterCDO.getId());
        return chapterCDO;
    }

    public String insertContentLinks(String content, List<ChapterImageLink> chapterImageLinks) {
        // 将字符串按行分割
        String[] lines = content.split("\n");
        List<String> resultLines = new ArrayList<>();

        // 按照位置插入 contentLink
        for (int i = 0; i < lines.length; i++) {
            resultLines.add(lines[i]);

            // 检查当前行是否需要插入 contentLink
            for (ChapterImageLink imageLink : chapterImageLinks) {
                String location = imageLink.getLocation();
                if (location != null && location.contains("_")) {
                    String[] parts = location.split("_");
                    int numerator = Integer.parseInt(parts[0]);
                    int denominator = Integer.parseInt(parts[1]);
                    int position = (int) Math.round((double) numerator / denominator * lines.length);

                    // 如果当前行是插入位置，插入 contentLink
                    if (i == position) {
                        resultLines.add(imageLink.getContentLink());
                    }
                }
            }
        }

        // 返回插入后的字符串
        return String.join("\n", resultLines);
    }

    public Chapter findChapterByChapterId(Long id) {
        return chapterRepository.findByIdAndIsDeletedFalse(id);
    }

    public Page<ChapterProjection> getChaptersByNovelIdWithPagination(Long novelId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chapterRepository.findAllByNovelIdAndIsDeletedFalse(novelId, pageable);
    }

    public List<ChapterProjection> getChaptersByNovelId(Long novelId) {
        return chapterRepository.findAllByNovelIdAndIsDeletedFalse(novelId);
    }

    public List<Long> findIdsByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(Long novelId) {
        return chapterRepository.findIdsByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(novelId);
    }

    public Integer findChapterNumberById(Long id){
        return chapterRepository.findChapterNumberById(id);
    }

    @Transactional
    public void test() {
        Chapter chapter = chapterRepository.getReferenceById(6868L);
        ChapterCopy chapterCopy = new ChapterCopy();
        chapterCopy.setChapterNumber(chapter.getChapterNumber());
        chapterCopy.setContent(chapter.getContent());
        chapterCopy.setNovelId(chapter.getNovelId());
        chapterCopy.setTitle(chapter.getTitle());
        chapterCopy.setDeleted(false);
        chapterCopy.setOwnPhoto(false);
        chapterCopy.setUpdatedAt(chapter.getUpdatedAt());
        chapterCopyRepository.save(chapterCopy);
        ChapterCopy referenceById = chapterCopyRepository.getReferenceById(chapterCopy.getId());
        System.out.println(referenceById.getContent());
    }


    public String getRandomChapter() {
        // 1. 随机取一条小说
        Novel novel = novelRepository.findRandomNovel();
        // 2. 随机取章节，最多尝试 5 次
        Chapter chapter = null;
        int attempts = 0;
        while (attempts < 5) {
            chapter = chapterRepository.findRandomChapterByNovelId(novel.getId());
            if (chapter != null && chapter.getContent() != null && chapter.getContent().length() >= 200) {
                break; // 找到符合条件的章节
            }
            attempts++;
        }
        assert chapter != null;
        String content = chapter.getContent();
        return content.replaceAll("[^\\u4E00-\\u9FA5]", "");
    }

    public List<UserIdUsernameDTO> findAllContentVersion(Long chapterId) {
        return userChapterEditRepository.findUserIdAndUsernameByNovelIdAndChapterId(chapterId);
    }

    public void saveModifyContent(User user, Note note) {
        Optional<UserChapterEdit> byUserIdAndChapterId = userChapterEditRepository.findByUserIdAndChapterId(user.getId(), note.getChapterId());
        if (byUserIdAndChapterId.isPresent()) {
            UserChapterEdit userChapterEdit = byUserIdAndChapterId.get();
            userChapterEdit.setContent(applyFontMapFast(note.getContent(),obfuscateFontOTF.reversedFontMap));
            userChapterEditRepository.save(userChapterEdit);
        } else {
            UserChapterEdit userChapterEdit = new UserChapterEdit();
            userChapterEdit.setUserId(user.getId());
            userChapterEdit.setUsername(user.getEmail());
            userChapterEdit.setChapterId(note.getChapterId());
            userChapterEdit.setContent(applyFontMapFast(note.getContent(),obfuscateFontOTF.reversedFontMap));
            userChapterEditRepository.save(userChapterEdit);
        }
    }

    public void processTerminology(TerminologyModifyCTO cto, Map<String, String> targetToModify) {
        List<Long> chapterIds;
        if (cto.getChapterId()==null) {
            chapterIds = chapterRepository.findIdsByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(cto.getNovelId());
        } else {
            chapterIds = new ArrayList<>();
            chapterIds.add(cto.getChapterId());
        }
        int batchSize = 10;
        List<Chapter> chapterList = new ArrayList<>();
        List<ChapterScalingUpOne> chapterScalingUpOneList = new ArrayList<>();
        for (int i = 0; i < chapterIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, chapterIds.size());
            List<Long> batchIds = chapterIds.subList(i, end);

            // 4. 批量查询实体
            ChapterSyncCTO chapterSyncCTO = findChapterSyncCTOByIdAndIsDeletedFalse(batchIds);
            List<Chapter> chapters = chapterSyncCTO.getChapters();
            List<ChapterScalingUpOne> chapterScalingUpOnes = chapterSyncCTO.getChapterScalingUpOnes();
            
            // 确保返回的列表不为 null
            if (chapters == null) {
                log.warn("批量查询章节返回 null，batchIds={}", batchIds);
                chapters = new ArrayList<>();
            }
            if (chapterScalingUpOnes == null) {
                log.warn("批量查询从库章节返回 null，batchIds={}", batchIds);
                chapterScalingUpOnes = new ArrayList<>();
            }
            
            // 5. 逐章替换 content
            chapters.forEach(ch -> {
                if (ch == null) {
                    log.warn("批量查询返回的章节包含 null 元素");
                    return;
                }
                String content = ch.getContent();
                if (content == null || content.isEmpty()) return;
                for (Map.Entry<String, String> e : targetToModify.entrySet()) {
                    content = content.replace(e.getKey(), e.getValue());
                }
                ch.setContent(content);
            });
            // 6. 批量保存
            chapterList.addAll(chapters);

            chapterScalingUpOnes.forEach(ch -> {
                if (ch == null) {
                    log.warn("批量查询返回的从库章节包含 null 元素");
                    return;
                }
                String content = ch.getContent();
                if (content == null || content.isEmpty()) return;
                for (Map.Entry<String, String> e : targetToModify.entrySet()) {
                    content = content.replace(e.getKey(), e.getValue());
                }
                ch.setContent(content);
            });
            // 6. 批量保存
            chapterScalingUpOneList.addAll(chapterScalingUpOnes);
        }
        chapterRepository.saveAll(chapterList);
        // 只有在扩展数据库可用时才保存，并添加额外的类型检查
        if (chapterScalingUpOneRepository != null && !chapterScalingUpOneList.isEmpty()) {
            try {
                chapterScalingUpOneRepository.saveAll(chapterScalingUpOneList);
                log.info("成功保存 {} 条从库章节", chapterScalingUpOneList.size());
            } catch (Exception e) {
                log.error("保存从库章节失败", e);
                throw e;
            }
        }
    }


    public ChapterSyncCTO findChapterSyncCTOByIdAndIsDeletedFalse(List<Long> chapterIds) {
        // 参数校验
        if (chapterIds == null || chapterIds.isEmpty()) {
            log.warn("查询章节同步数据时传入空的章节ID列表");
            return new ChapterSyncCTO(new ArrayList<>(), new ArrayList<>());
        }
        
        List<ChapterSync> syncedList = chapterSyncRepository.findByChapterIdInAndSyncedTrue(chapterIds);
        List<Long> list = syncedList.stream().map(ChapterSync::getChapterId).toList();
        List<ChapterScalingUpOne> chapterScalingUpOneList = new ArrayList<>();
        
        // 只有在扩展数据库可用时才查询，并添加类型安全检查
        if (chapterScalingUpOneRepository != null && !list.isEmpty()) {
            try {
                List<ChapterScalingUpOne> fetchedList = chapterScalingUpOneRepository.findAllById(list);
                if (fetchedList != null) {
                    chapterScalingUpOneList = fetchedList;
                    // 验证返回的数据ID是否在预期范围内
                    long invalidCount = chapterScalingUpOneList.stream()
                        .filter(ch -> ch == null || !list.contains(ch.getId()))
                        .count();
                    if (invalidCount > 0) {
                        log.warn("从库查询返回了 {} 条无效数据", invalidCount);
                    }
                } else {
                    log.warn("从库查询返回 null，使用空列表");
                }
            } catch (Exception e) {
                log.error("从库批量查询失败，chapterIds={}", list, e);
                // 继续执行，只是不使用从库数据
            }
        }
        
        // 2. 取出它们的 chapterId
        Set<Long> syncedIds = syncedList.stream().map(ChapterSync::getChapterId).collect(Collectors.toSet());
        List<Long> unSyncedIds = chapterIds.stream()
                .filter(id -> !syncedIds.contains(id))
                .toList();
        List<Chapter> chapterList = chapterRepository.findAllById(unSyncedIds);
        
        if (chapterList == null) {
            log.warn("主库查询返回 null，使用空列表");
            chapterList = new ArrayList<>();
        }
        
        return new ChapterSyncCTO(chapterList, chapterScalingUpOneList);
    }
}