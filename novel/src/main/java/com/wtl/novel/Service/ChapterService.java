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

@Service
public class ChapterService {

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

    public ChapterCDO findByIdAndIsDeletedFalse(Long id) {
        Optional<ChapterSync> chapterSync = chapterSyncRepository.findByChapterId(id);
        Chapter byIdAndIsDeletedFalse = chapterRepository.findByIdAndIsDeletedFalse(id);
        // 如果未配置扩展数据库或章节未同步，从主库读取
        if (chapterScalingUpOneRepository == null || chapterSync.isEmpty() || !chapterSync.get().getSynced()) {
            byIdAndIsDeletedFalse.setContent(StringEncoder.cleanText(byIdAndIsDeletedFalse.getContent()));
            return new ChapterCDO(byIdAndIsDeletedFalse);
        } else if (chapterSync.get().getHostServerName().equals("host1") && chapterSync.get().getSynced()) {
            ChapterScalingUpOne chapterScalingUpOne = chapterScalingUpOneRepository.findById(byIdAndIsDeletedFalse.getId()).get();
            return new ChapterCDO(chapterScalingUpOne);
        } else {
            return new ChapterCDO(new ChapterScalingUpOne());
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
        List<Chapter> chapterList = new ArrayList<>();;
        List<ChapterScalingUpOne> chapterScalingUpOneList = new ArrayList<>();;
        for (int i = 0; i < chapterIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, chapterIds.size());
            List<Long> batchIds = chapterIds.subList(i, end);

            // 4. 批量查询实体
            ChapterSyncCTO chapterSyncCTO = findChapterSyncCTOByIdAndIsDeletedFalse(batchIds);
            List<Chapter> chapters = chapterSyncCTO.getChapters();
            List<ChapterScalingUpOne> chapterScalingUpOnes = chapterSyncCTO.getChapterScalingUpOnes();
            // 5. 逐章替换 content
            chapters.forEach(ch -> {
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
        // 只有在扩展数据库可用时才保存
        if (chapterScalingUpOneRepository != null && !chapterScalingUpOneList.isEmpty()) {
            chapterScalingUpOneRepository.saveAll(chapterScalingUpOneList);
        }
    }


    public ChapterSyncCTO findChapterSyncCTOByIdAndIsDeletedFalse(List<Long> chapterIds) {
        List<ChapterSync> syncedList = chapterSyncRepository.findByChapterIdInAndSyncedTrue(chapterIds);
        List<Long> list = syncedList.stream().map(ChapterSync::getChapterId).toList();
        List<ChapterScalingUpOne> chapterScalingUpOneList = new ArrayList<>();
        // 只有在扩展数据库可用时才查询
        if (chapterScalingUpOneRepository != null) {
            chapterScalingUpOneList = chapterScalingUpOneRepository.findAllById(list);
        }
        // 2. 取出它们的 chapterId
        Set<Long> syncedIds = syncedList.stream().map(ChapterSync::getChapterId).collect(Collectors.toSet());
        List<Long> unSyncedIds = chapterIds.stream()
                .filter(id -> !syncedIds.contains(id))
                .toList();
        List<Chapter> chapterList = chapterRepository.findAllById(unSyncedIds);
        return new ChapterSyncCTO(chapterList,chapterScalingUpOneList);
    }
}