package com.wtl.novel.Service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtl.novel.CDO.*;
import com.wtl.novel.entity.*;
import com.wtl.novel.repository.*;
import com.wtl.novel.util.TextImgExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ShuTuNovelChapterService {
    @Autowired
    private ShuTuNovelChapterRepository shuTuNovelChapterRepository;
    @Autowired
    private ChapterExecuteRepository chapterExecuteRepository;
    @Autowired
    private ChapterImageLinkRepository chapterImageLinkRepository;
    @Autowired
    private PlatformRepository platformRepository;
    @Autowired
    private NovelService novelService;
    @Autowired
    private NovelRepository novelRepository;
    @Autowired
    private NovelTagRepository novelTagRepository;
    @Autowired
    private UserNovelRelationService userNovelRelationService;
    @Autowired
    private UserNovelRelationRepository userNovelRelationRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private UserOperationLogService userOperationLogService;
    @Autowired
    private DictionaryRepository dictionaryRepository;

    private static final Pattern NOVEL_ID_PATTERN =
            Pattern.compile("https?://[^/]+/novel/(\\d+)(?:\\?.*)?", Pattern.CASE_INSENSITIVE);
    private static String platformName = "shutu";
    public static String extractNovelId(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        Matcher m = NOVEL_ID_PATTERN.matcher(url);
        return m.matches() ? m.group(1) : null;
    }

    @Transactional
    public Boolean updateShuTu(BookInfo bookInfo, User userByToken) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String trueId = extractNovelId(bookInfo.getBookUrl());
            List<Novel> novels = novelRepository.findAllByTrueId(platformName + "_" + trueId);
            if (novels.isEmpty()) {
                return false;
            }

            Platform platformByPlatformName = platformRepository.findPlatformByPlatformName(platformName);


            List<BookInfo.ChapterInfo> chapters = bookInfo.getChapters();
            if (chapters == null || chapters.isEmpty()) {
                return true;
            }

            Novel novel = novels.get(0);
            if (!novel.getPlatform().equals(platformName)) {
                return false;
            }
            Long novelId = novel.getId();
            Long platformId = platformByPlatformName.getId();

            // 1. 一次性取出所有章节号
            Set<Integer> chapterNums = chapters.stream()
                    .map(BookInfo.ChapterInfo::getChapterNum)
                    .collect(Collectors.toSet());

            // 2. 查询已存在的
            List<ShuTuNovelChapter> existList =
                    shuTuNovelChapterRepository.findByNovelIdAndChapterNumIn(novelId, chapterNums);
            Set<Integer> existNums = existList.stream()
                    .map(ShuTuNovelChapter::getChapterNum)
                    .collect(Collectors.toSet());

            // 3. 过滤+组装待插入数据
            List<ShuTuNovelChapter> toSave = chapters.stream()
                    .filter(c -> !existNums.contains(c.getChapterNum()))
                    .map(c -> {
                        c.setChapterUrl(extractNovelId(c.getChapterUrl()));
                        return new ShuTuNovelChapter(c, novelId, platformId);
                    })
                    .collect(Collectors.toList());

            // 4. 批量保存
            if (!toSave.isEmpty()) {
                shuTuNovelChapterRepository.saveAll(toSave);
            }

            userOperationLogService.addLog(userByToken.getId(), "updateShuTu", bookInfo.getBookUrl() + "_" + bookInfo.getChapters().size());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean saveContent(ChapterContent chapterContent, User userByToken) {
        try {
            int chapterNum = chapterContent.getChapterNum();
            String trueId = extractNovelId(chapterContent.getBookUrl());
            List<Novel> novels = novelRepository.findAllByTrueId(platformName + "_" + trueId);
            if (novels.isEmpty()) {
                return false;
            }
            Novel novel = novels.get(0);
            Optional<ShuTuNovelChapter> byNovelIdAndChapterNum = shuTuNovelChapterRepository.findByNovelIdAndChapterNum(novel.getId(), chapterNum);
            if (byNovelIdAndChapterNum.isEmpty()){
                return false;
            }
            ShuTuNovelChapter shuTuNovelChapter = byNovelIdAndChapterNum.get();
            if (shuTuNovelChapter.getDownloaded()) {
                return false;
            }

            ChapterExecute chapterNumberAndIsDeletedFalse = chapterExecuteRepository.findByNovelIdAndChapterNumberAndIsDeletedFalse(novel.getId(), chapterContent.getChapterNum());
            if (chapterNumberAndIsDeletedFalse != null) {
                chapterExecuteRepository.delete(chapterNumberAndIsDeletedFalse);
            }

            TextImgExtractor.Result process = TextImgExtractor.process(chapterContent.getContent());
            String textNoImg = process.textNoImg;
            textNoImg = processText(textNoImg);
            Map<String, String> imgWithPos = process.imgWithPos;
            boolean photo = false;
            if (!imgWithPos.isEmpty()) {
                photo = true;
            }

            shuTuNovelChapter.setDownloaded(true);
            String epFormat = "EP%s";
            String episodeFile = String.format(epFormat, String.format("%05d", chapterNum));
            ChapterExecute chapterExecute = new ChapterExecute(novel.getId(),episodeFile, chapterNum, textNoImg,0,platformName + "_" + shuTuNovelChapter.getChapterTrueId(),photo);
            chapterExecuteRepository.save(chapterExecute);

            List<ChapterImageLink> chapterImageLinks = new ArrayList<>();
            imgWithPos.forEach((img, pos) ->{
                ChapterImageLink chapterImageLink = new ChapterImageLink(chapterExecute.getTrueId(), img, pos);
                chapterImageLinks.add(chapterImageLink);
            });

            chapterImageLinkRepository.saveAll(chapterImageLinks);

            shuTuNovelChapterRepository.save(shuTuNovelChapter);
            chapterContent.setContent("");
            ObjectMapper mapper = new ObjectMapper();
            String ctoJson = null;
            ctoJson = mapper.writeValueAsString(chapterContent);
            userOperationLogService.addLog(userByToken.getId(), "saveContent", ctoJson);
            return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }

    }


    public static String processText(String textNoImg) {
        if (textNoImg != null && !textNoImg.isEmpty()) {
            // 按行分割文本
            String[] lines = textNoImg.split("\n");
            StringBuilder result = new StringBuilder();

            for (String line : lines) {
                if (line.length() > 1500) {
                    // 使用正则表达式分割但保留分隔符
                    String[] subLines = line.split("(?<=[”’*])");

                    for (int i = 0; i < subLines.length; i++) {
                        if (!subLines[i].trim().isEmpty()) {
                            result.append(subLines[i].trim());

                            // 如果这一行以分隔符结尾，就添加换行
                            if (subLines[i].matches(".*[”’*]$") && i < subLines.length - 1) {
                                result.append("\n");
                            }
                        }
                    }
                } else {
                    // 不超过1500字，直接保留原行
                    result.append(line);
                }
                result.append("\n");
            }

            // 去除最后多余的空行
            if (!result.isEmpty()) {
                result.setLength(result.length() - 1);
            }

            textNoImg = result.toString();
            return textNoImg;
        }
        return textNoImg;
    }

    public Page<BookMetaDto> getAllNoDownloadInfo(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<UserNovelRelation> pageData =
                userNovelRelationRepository.findByUserIdAndPlatformOrderByCreatedAtDesc(userId, platformName,
                        pageRequest);
        String valueField = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("shutuNovel").getValueField();
        List<UserNovelRelation> content = pageData.getContent();
        List<Long> novelIds = content.stream().map(UserNovelRelation::getNovelId).toList();
        List<Novel> allByIdIn = novelRepository.findAllByIdIn(novelIds);
        List<BookMetaDto> bookMetaDtos = new ArrayList<>();
        for (Novel novel : allByIdIn) {
            BookMetaDto bookMetaDto = new BookMetaDto();
            List<Integer> noDownloadInfo = getNoDownloadInfo(novel.getId());
            bookMetaDto.setNoGet(noDownloadInfo);
            bookMetaDto.setName(novel.getTitle());
            bookMetaDto.setBookUrl(valueField + novel.getTrueId().replace(platformName + "_",""));
            bookMetaDtos.add(bookMetaDto);
        }
        return new PageImpl<>(bookMetaDtos, pageData.getPageable(), pageData.getTotalElements());
    }

    public Page<BookMetaDto> getAllNoDownloadInfo(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Novel> allByIdIn = novelRepository.findByPlatformOrderByCreatedAtDesc(platformName, pageRequest);
        String valueField = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("shutuNovel").getValueField();
        List<BookMetaDto> bookMetaDtos = new ArrayList<>();
        for (Novel novel : allByIdIn) {
            BookMetaDto bookMetaDto = new BookMetaDto();
            List<Integer> noDownloadInfo = getNoDownloadInfo(novel.getId());
            bookMetaDto.setNoGet(noDownloadInfo);
            bookMetaDto.setName(novel.getTitle());
            bookMetaDto.setBookUrl(valueField + novel.getTrueId().replace(platformName + "_",""));
            bookMetaDtos.add(bookMetaDto);
        }
        return new PageImpl<>(bookMetaDtos, allByIdIn.getPageable(), allByIdIn.getTotalElements());
    }

    public List<Integer> getNoDownloadInfo(Long id) {
        Optional<Novel> byId = novelRepository.findById(id);
        if (byId.isEmpty()) {
            return new ArrayList<>();
        }
        Novel novel = byId.get();
        String platform = novel.getPlatform();
        if (!platform.equals(platformName)) {
            return new ArrayList<>();
        }
        List<ShuTuNovelChapter> chapterList = shuTuNovelChapterRepository.findAllByNovelIdAndDownloadedTrueOrderByChapterNum(novel.getId());
        if (chapterList.isEmpty()) {
            ArrayList<Integer> objects = new ArrayList<>();
            objects.add(0);
            return objects;
        }
        Set<Integer> exists = chapterList.stream()
                .map(ShuTuNovelChapter::getChapterNum)
                .collect(Collectors.toSet());

        int min = 1;
        int max = exists.isEmpty() ? 1 : exists.stream().max(Integer::compareTo).orElse(1);

        List<Integer> missing = new ArrayList<>(IntStream.rangeClosed(min, max)
                .filter(n -> !exists.contains(n))
                .boxed()
                .toList());
        missing.add(max + 1);
        return missing;
    }

    @Transactional
    public String saveNovel(ShuTuUploadMetadata metadata, Long userId) {
        if (metadata.getPlatform().equals("노벨피아")) {
            return "不能导入novelPia的书";
        }
        String trueId = platformName + "_" + metadata.getTrueId();
        List<Novel> novels = novelRepository.findAllByTrueId(trueId);
        if (!novels.isEmpty()) {
            return "本书已导入";
        }
        Novel novel = new Novel();
        novel.setTrueName(metadata.getTitle());
        novel.setTitle(metadata.getTitle());
        novel.setUp(0L);
        novel.setNovelLike(0L);
        novel.setNovelRead(0L);
        novel.setTrueId(trueId);
        novel.setRecommend(0L);
        novel.setAuthorName(metadata.getAuthor());
        novel.setAuthorId(metadata.getAuthor());
        novel.setFontNumber(0L);
        novel.setPlatform(platformName);
        novel.setPhotoUrl(null);
        Novel saveNovel = novelRepository.save(novel);

        // 添加汉化记录
        UserNovelRelation relation = new UserNovelRelation();
        relation.setUserId(userId);
        relation.setNovelId(saveNovel.getId());
        relation.setPlatform(platformName);
        userNovelRelationService.saveUserNovelRelation(relation);

        metadata.genTags();
        for (String tag : metadata.getTags()) {
            Tag tagToSave = new Tag(tag, platformName, tag);
            Optional<Tag> existingTag = tagRepository.findByPlatformAndTrueName(platformName,tagToSave.getTrueName());
            if (existingTag.isEmpty()) {
                Tag syosetuTag = tagRepository.save(tagToSave);
                novelTagRepository.save(new NovelTag(saveNovel.getId(), syosetuTag.getId()));
            } else {
                Tag tag1 = existingTag.get();
                novelTagRepository.save(new NovelTag(saveNovel.getId(), tag1.getId()));
            }
        }

        return "已导入";
    }

    public Page<BookMetaDto> searchNoDownloadInfo(String bookName, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Novel> novels = novelRepository.findByPlatformAndTitleContainingOrPlatformAndTrueNameContainingOrderByCreatedAtDesc(platformName, bookName, platformName, bookName, pageRequest);
        List<BookMetaDto> bookMetaDtos = new ArrayList<>();
        String valueField = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("shutuNovel").getValueField();
        for (Novel novel : novels.getContent()) {
            BookMetaDto bookMetaDto = new BookMetaDto();
            List<Integer> noDownloadInfo = getNoDownloadInfo(novel.getId());
            bookMetaDto.setNoGet(noDownloadInfo);
            bookMetaDto.setName(novel.getTitle());
            bookMetaDto.setBookUrl(valueField + novel.getTrueId().replace(platformName + "_",""));
            bookMetaDtos.add(bookMetaDto);
        }
        return new PageImpl<>(bookMetaDtos, novels.getPageable(), novels.getTotalElements());
    }
}
