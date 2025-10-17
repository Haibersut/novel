package com.wtl.novel.Service;

import com.wtl.novel.CDO.DiffDto;
import com.wtl.novel.CDO.NovelCTO;
import com.wtl.novel.CDO.NovelWithStatusDTO;
import com.wtl.novel.entity.*;
import com.wtl.novel.repository.*;
import com.wtl.novel.scalingUp.repository.ChapterScalingUpOneRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class NovelService {
    @Autowired
    private NovelRepository novelRepository;
    @Autowired(required = false)
    private ChapterScalingUpOneRepository chapterScalingUpOneRepository;
    @Autowired
    private ShuTuNovelChapterRepository shuTuNovelChapterRepository;
    @Autowired
    private ChapterExecuteRepository chapterExecuteRepository;
    @Autowired
    private UserNovelRelationRepository userNovelRelationRepository;
    @Autowired
    private TerminologyRepository terminologyRepository;
    @Autowired
    private PostService postService;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private NovelTagRepository novelTagRepository;
    @Autowired
    private FavoriteService favoriteService;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private ReadingRecordRepository readingRecordRepository;
    @Autowired
    private CredentialService credentialService;
    @Autowired
    private ReadingRecordService readingRecordService;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private UserTagFilterService userTagFilterService;
    @Autowired
    private TransactionTemplate txTemplate;
    @Autowired
    private UserOperationLogService userOperationLogService;
    @Autowired
    private UserOperationLogRepository userOperationLogRepository;
    @Autowired
    private DictionaryRepository dictionaryRepository;
    @Autowired
    private UserRepository userRepository;
    public int incrementFontNumberById(Long id, Long increment) {
        return novelRepository.incrementFontNumberById(id, increment);
    }

    public Page<Novel> getNovelsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return novelRepository.findAllNotDeletedWithPageable(pageable);
    }

    // 根据标签查小说
    public Page<Novel> getNovelsByTagIdWithPagination(Long tagId, Pageable pageable) {
        Page<NovelTag> byId = novelTagRepository.findById(tagId, pageable);
        List<Long> novelIdList = byId.map(NovelTag::getNovelId).toList();
        return new PageImpl<>(novelRepository.findAllById(novelIdList), byId.getPageable(), byId.getTotalElements());
    }

    public List<Tag> getTagsByNovelId( Long novelId) {
        List<NovelTag> byNovelId = novelTagRepository.findByNovelId(novelId);
        return tagRepository.findByIdIn(byNovelId.stream().map(NovelTag::getTagId).toList());
    }

    // 分页查询小说
//    public Page<Novel> getNovelsWithPagination(Long tagId, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        if (tagId == 0) {
//            return novelRepository.findAllByIsDeletedFalseOrderByUpDesc(pageable);
//        } else if (tagId == 4) {
//            List<Long> tags = new ArrayList<>();
//            tags.add(1L);
//            List<Long> novelIdList = novelTagRepository.findDistinctNovelIdByTagIdNotIn(tags);
//            return novelRepository.findByIdInAndIsDeletedFalseOrderByUpDesc(novelIdList, pageable);
//        } else {
//            List<NovelTag> byTagId = novelTagRepository.findByTagId(tagId);
//            List<Long> novelIdList = byTagId.stream().map(NovelTag::getNovelId).toList();
//            return novelRepository.findByIdInAndIsDeletedFalseOrderByUpDesc(novelIdList, pageable);
//        }
//    }

//    public Page<Novel> findByTitleContainingOrTrueNameContaining(String keyword,Long userId) {
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<Novel> novels = novelRepository.findByTitleContainingAndIsDeletedFalseOrTrueNameContainingAndIsDeletedFalseOrderByUpDesc(keyword, keyword, pageable);
//        List<Novel> novelsContent = novels.getContent();
//        List<Long> novelIds = novelsContent.stream().map(Novel::getId).toList();
//        List<ReadingRecord> readingRecords = readingRecordService.getReadingRecordsByBookIds(userId, novelIds);
//        List<Favorite> favorites = favoriteRepository.findAllByUserId(userId);
//        return novels;
//    }

    public Page<NovelWithStatusDTO> findByTitleContainingOrTrueNameContaining(String keyword, Long userId) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Novel> novels = novelRepository.findByTitleContainingAndIsDeletedFalseOrTrueNameContainingAndIsDeletedFalseOrderByUpDesc(
                keyword, keyword, pageable
        );

        List<Novel> novelsContent = novels.getContent();
        List<Long> novelIds = novelsContent.stream().map(Novel::getId).toList();

        // 获取阅读记录和收藏记录
        List<ReadingRecord> readingRecords = readingRecordService.getReadingRecordsByBookIds(userId, novelIds);
        List<Favorite> favorites = favoriteRepository.findAllByUserId(userId);

        // 获取用户过滤标签和小说标签
        List<UserTagFilter> filterTag = userTagFilterService.getFilterTag(userId);
        List<Long> filterTagIds = filterTag.stream()
                .map(UserTagFilter::getTagId)
                .toList();

        List<NovelTag> novelTags = novelTagRepository.findAllByNovelIdIn(novelIds);

        // 构建小说ID到标签ID列表的映射
        Map<Long, List<Long>> novelTagMap = novelTags.stream()
                .collect(Collectors.groupingBy(
                        NovelTag::getNovelId,
                        Collectors.mapping(NovelTag::getTagId, Collectors.toList())
                ));

        // 过滤掉包含用户屏蔽标签的小说
        List<Novel> filteredNovels = novelsContent.stream()
                .filter(novel -> {
                    List<Long> tags = novelTagMap.getOrDefault(novel.getId(), Collections.emptyList());
                    // 保留没有标签或标签不包含任何过滤标签的小说
                    return tags.isEmpty() || Collections.disjoint(tags, filterTagIds);
                })
                .toList();

        // 转换为DTO
        List<NovelWithStatusDTO> dtoList = filteredNovels.stream().map(novel -> {
            NovelWithStatusDTO dto = new NovelWithStatusDTO(novel);

            // 设置最后阅读章节
            readingRecords.stream()
                    .filter(record -> record.getNovelId().equals(novel.getId()))
                    .findFirst()
                    .ifPresent(record -> dto.setLastChapter(record.getLastChapter()));

            // 检查是否收藏
            boolean isFav = favorites.stream()
                    .anyMatch(fav -> fav.getObjectId().equals(novel.getId()));
            dto.setFavorite(isFav);

            return dto;
        }).toList();

        // 返回分页结果（注意总数需要调整）
        return new PageImpl<>(dtoList, pageable, dtoList.size());
    }

    public boolean updateTitleById(String title, Long id) {
        novelRepository.updateTitleById(id, title);
        return true;
    }

    // 获取特定小说的信息
    public NovelCTO findNovelById(Long id, HttpServletRequest httpRequest) {
        Novel byIdAndIsDeletedFalse = novelRepository.findByIdAndIsDeletedFalse(id);
        try {
            String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
            String authorizationHeader = authorizationInfo[0];
            Credential credential = credentialService.findByToken(authorizationHeader);
            ReadingRecord byUserIdAndNovelId = readingRecordRepository.findByUserIdAndNovelId(credential.getUser().getId(), id);
            String name = favoriteService.getFavoriteGroupNameByObjectId(credential.getUser().getId(), id);
            NovelCTO novelCTO = new NovelCTO(byIdAndIsDeletedFalse, byUserIdAndNovelId.getLastChapter(), byUserIdAndNovelId.getLastChapterId());
            novelCTO.setFavoriteGroup(name);
            return novelCTO;
        } catch (Exception e) {
            return new NovelCTO(byIdAndIsDeletedFalse, null, null);
        }
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

    @Transactional
    @Scheduled(cron = "0 0 4 * * ?")
    public void executeRecommend() {
        Map<Long, Long> longLongMap = postService.calcScoreByCollections();

        // 分批处理避免SQL语句过长
        int batchSize = 1000;
        List<Long> novelIds = new ArrayList<>(longLongMap.keySet());

        for (int i = 0; i < novelIds.size(); i += batchSize) {
            List<Long> batchIds = novelIds.subList(i, Math.min(i + batchSize, novelIds.size()));

            // 构建批量更新SQL
            StringBuilder updateSql = new StringBuilder(
                    "UPDATE Novel n SET n.recommend = CASE n.id "
            );

            // 添加WHEN THEN子句
            for (Long novelId : batchIds) {
                updateSql.append("WHEN ")
                        .append(novelId)
                        .append(" THEN ")
                        .append(longLongMap.get(novelId))
                        .append(" ");
            }

            updateSql.append("ELSE n.recommend END WHERE n.id IN :novelIds");

            // 执行批量更新
            entityManager.createQuery(updateSql.toString())
                    .setParameter("novelIds", batchIds)
                    .executeUpdate();
        }
    }

    private static final ConcurrentHashMap<Long, ReentrantLock> LOCK_POOL = new ConcurrentHashMap<>();

    public Integer increaseUp(Long id, User user, String type,
                              String favoriteType, Long groupId) {

        ReentrantLock lock = LOCK_POOL.computeIfAbsent(user.getId(), k -> new ReentrantLock());
        if (!lock.tryLock()) return 0;

        try {
            // 用实例方法调用，不再是静态
            return txTemplate.execute(status -> doIncreaseUp(id, user, type, favoriteType, groupId));
        } finally {
            lock.unlock();
        }
    }

    // 收藏功能
    @Transactional
    public Integer doIncreaseUp(Long id, User user, String type, String favoriteType, Long groupId) {
        // 查询小说是否存在且未被删除
        Novel novel = novelRepository.findByIdAndIsDeletedFalse(id);
        if (novel == null) {
            return 0; // 小说不存在或已被删除，返回0
        }

        List<Favorite> favoriteNovel = favoriteRepository.findByUserIdAndObjectIdAndFavoriteType(user.getId(), novel.getId(), favoriteType);

        // 如果用户已经收藏过且当前操作是收藏，则不做任何处理
        if (!favoriteNovel.isEmpty() && type.equals("up")) {
            return 0;
        }
        // 如果用户未收藏过且当前操作是取消收藏，则不做任何处理
        else if (favoriteNovel.isEmpty() && type.equals("down")) {
            return 0;
        }

        // 根据操作类型处理收藏或取消收藏
        if (type.equals("up")) { // 收藏
            novel.setUp(novel.getUp() + 1); // 增加收藏数
            novelRepository.save(novel); // 保存小说信息

            // 创建收藏记录
            Favorite favorite = new Favorite(user.getId(), favoriteType, novel.getId(), novel.getTitle());
            favorite.setGroupId(groupId);
            favoriteRepository.save(favorite);

            return 1; // 返回1表示收藏成功
        } else if (type.equals("down")) { // 取消收藏
            novel.setUp(novel.getUp() - 1); // 减少收藏数
            novelRepository.save(novel); // 保存小说信息

            // 删除收藏记录
            favoriteRepository.deleteByUserIdAndObjectIdAndFavoriteType(user.getId(), novel.getId(), favoriteType);
            return -1; // 返回-1表示取消收藏成功
        }
        return 0; // 其他情况返回0
    }


//    ===
    // 分页查询小说
//    public Page<Novel> getNovelsWithPaginationByArgs(String platform, String novelType,String fontNumber, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        String[] parts = fontNumber.split("_");
//
//        // 检查是否分隔成功且有两部分
//        if (parts.length != 2) {
//            throw new IllegalArgumentException("Invalid input format. Expected format: 'number_number'");
//        }
//
//        // 将字符串转换为 Long
//        Long firstNumber = Long.parseLong(parts[0]);
//        Long secondNumber = Long.parseLong(parts[1]);
//        if (novelType.equals("全部")) {
//            return novelRepository.findByPlatformAndFontNumberBetweenAndIsDeletedFalseOrderByUpDesc(platform,firstNumber,secondNumber,pageable);
//        }
//
//        return novelRepository.findByPlatformAndNovelTypeAndFontNumberBetweenAndIsDeletedFalseOrderByUpDesc(platform, novelType,firstNumber,secondNumber,pageable);
//    }


    public Map<Long, List<String>> getTagsForNovels(List<Long> novelIds) {
        // Step 1: Query the NovelTag table to get all NovelTag entities for the given novel IDs.
        List<NovelTag> novelTags = novelTagRepository.findAllByNovelIdIn(novelIds);

        // Step 2: Extract all unique tag IDs from the retrieved NovelTag entities.
        List<Long> tagIds = novelTags.stream()
                .map(NovelTag::getTagId)
                .distinct()
                .collect(Collectors.toList());

        // Step 3: If there are no tags, return an empty map.
        if (tagIds.isEmpty()) {
            return new HashMap<>();
        }

        // Step 4: Query the Tag table to get all Tag entities for the unique tag IDs.
        List<Tag> tags = tagRepository.findByIdIn(tagIds);

        // Step 5: Create a map for efficient lookup of tag name by tag ID.
        Map<Long, String> tagNameMap = tags.stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName));

        // Step 6: Create the final result map.
        Map<Long, List<String>> result = new HashMap<>();
        for (Long novelId : novelIds) {
            result.put(novelId, new java.util.ArrayList<>());
        }

        // Step 7: Iterate through the NovelTag list and populate the result map with tag names.
        for (NovelTag novelTag : novelTags) {
            Long novelId = novelTag.getNovelId();
            Long tagId = novelTag.getTagId();
            String tagName = tagNameMap.get(tagId);

            if (tagName != null) {
                // Get the list for the current novelId and add the tag name.
                result.get(novelId).add(tagName);
            }
        }
        return result;
    }

    public List<NovelCTO> findByAuthorId(String authorName,Long userId) {
        List<NovelCTO> page = novelRepository.findByAuthorNameAndIsDeletedFalse(authorName);
        if (userId != null) {
            List<ReadingRecord> records = readingRecordService.getReadingRecordsByBookIds(userId, page.stream().map(NovelCTO::getId).toList());
            populateReadingRecordInfo(page, records);
        }
        return page;
    }

    public List<NovelCTO> convertNpNovel(List<Novel> novels) {
        List<NovelCTO> novelCTOS = novels.stream().map(novel -> {
            return new NovelCTO(novel, null, null);
        }).toList();
        List<Long> novelIds = novelCTOS.stream().map(NovelCTO::getId).toList();

        Map<Long, Integer> longIntegerMap = chapterRepository.getChapterCountsByNovelIds(novelIds);
        Map<Long, List<String>> tagsForNovels = getTagsForNovels(novelIds);
        novelCTOS.forEach(novelCTO -> {
            novelCTO.setTags(tagsForNovels.getOrDefault(novelCTO.getId(), new ArrayList<>()));
            novelCTO.setChapterNum(longIntegerMap.getOrDefault(novelCTO.getId(), 0));
        });
        return novelCTOS;
    }


    public Page<NovelCTO> getNovelsWithPagination(String platform, String fontNumber, String tagIdStr, Pageable pageable, Long userId) {
        List<Long> tagIdList = convertToLongList(tagIdStr);
        String[] parts = fontNumber.split("_");
        // 检查是否分隔成功且有两部分
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid input format. Expected format: 'number_number'");
        }
        // 将字符串转换为 Long
        Long firstNumber = Long.parseLong(parts[0]);
        Long secondNumber = Long.parseLong(parts[1]);
        if (tagIdList.contains(0L)) {
            Page<NovelCTO> page = novelRepository.findByNovelCTOPlatformAndFontNumberBetweenAndIsDeletedFalse(platform, firstNumber, secondNumber, pageable);

            // 获取用户过滤标签和小说标签
            List<UserTagFilter> filterTag = userTagFilterService.getFilterTag(userId);
            List<Long> filterTagIds = filterTag.stream()
                    .map(UserTagFilter::getTagId)
                    .toList();
            List<Long> novelIds = page.getContent().stream().map(NovelCTO::getId).toList();
            List<NovelTag> novelTags = novelTagRepository.findAllByNovelIdIn(novelIds);
            Map<Long, List<Long>> novelTagMap = novelTags.stream()
                    .collect(Collectors.groupingBy(
                            NovelTag::getNovelId,
                            Collectors.mapping(NovelTag::getTagId, Collectors.toList())
                    ));
            // 过滤掉包含用户屏蔽标签的小说
            List<NovelCTO> filteredNovels = page.getContent().stream()
                    .filter(novel -> {
                        List<Long> tags = novelTagMap.getOrDefault(novel.getId(), Collections.emptyList());
                        // 保留没有标签或标签不包含任何过滤标签的小说
                        return tags.isEmpty() || Collections.disjoint(tags, filterTagIds);
                    })
                    .toList();
            page = new PageImpl<>(filteredNovels, pageable, page.getTotalElements());

            if (userId != null) {
                List<ReadingRecord> records = readingRecordService.getReadingRecordsByBookIds(userId, page.getContent().stream().map(NovelCTO::getId).toList());
                populateReadingRecordInfo(page.getContent(), records);
            }
            return page;
        }  else {
            List<Long> novelIdList = novelTagRepository.findNovelIdsByAllTagIds(tagIdList, tagIdList.size());
            Page<NovelCTO> page = novelRepository.findNovelCTOByNovelIdsAndPlatformAndFontNumberRange(novelIdList, platform, firstNumber, secondNumber, pageable);


            // 获取用户过滤标签和小说标签
            List<UserTagFilter> filterTag = userTagFilterService.getFilterTag(userId);
            List<Long> filterTagIds = filterTag.stream()
                    .map(UserTagFilter::getTagId)
                    .toList();
            List<Long> novelIds = page.getContent().stream().map(NovelCTO::getId).toList();
            List<NovelTag> novelTags = novelTagRepository.findAllByNovelIdIn(novelIds);
            Map<Long, List<Long>> novelTagMap = novelTags.stream()
                    .collect(Collectors.groupingBy(
                            NovelTag::getNovelId,
                            Collectors.mapping(NovelTag::getTagId, Collectors.toList())
                    ));
            // 过滤掉包含用户屏蔽标签的小说
            List<NovelCTO> filteredNovels = page.getContent().stream()
                    .filter(novel -> {
                        List<Long> tags = novelTagMap.getOrDefault(novel.getId(), Collections.emptyList());
                        // 保留没有标签或标签不包含任何过滤标签的小说
                        return tags.isEmpty() || Collections.disjoint(tags, filterTagIds);
                    })
                    .toList();
            page = new PageImpl<>(filteredNovels, pageable, page.getTotalElements());


            if (userId != null) {
                List<ReadingRecord> records = readingRecordService.getReadingRecordsByBookIds(userId, page.getContent().stream().map(NovelCTO::getId).toList());
                populateReadingRecordInfo(page.getContent(), records);
            }
            return page;
        }
    }

    public void populateReadingRecordInfo(List<NovelCTO> novelCTOs, List<ReadingRecord> readingRecords) {
        // 创建一个Map，用于按小说ID快速查找对应的阅读记录
        Map<Long, ReadingRecord> readingRecordMap = new HashMap<>();
        for (ReadingRecord record : readingRecords) {
            readingRecordMap.put(record.getNovelId(), record);
        }

        // 遍历小说列表，填充阅读记录信息
        for (NovelCTO novelCTO : novelCTOs) {
            Long novelId = novelCTO.getId();
            ReadingRecord record = readingRecordMap.get(novelId);
            if (record != null) {
                novelCTO.setLastChapter(record.getLastChapter());
                novelCTO.setLastChapterId(record.getLastChapterId());
            }
        }
    }

    // 将逗号分隔的字符串转换为Long列表，并去重
    public static List<Long> convertToLongList(String ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return Stream.of(ids.split(","))
                .map(id -> id.trim().isEmpty() ? null : Long.parseLong(id))
                .filter(Objects::nonNull)
                .distinct() // 去重
                .collect(Collectors.toList());
    }

    @Transactional
    public String repeatTrNovel(Long novelId, Long userId) {
        try {
            User userById = userRepository.findUserById(userId);
            if (userById.getPoint() < 500) {
                return "积分不足500，无权操作";
            }
            List<UserOperationLog> repeatTrNovel = userOperationLogService.getTodayLogs(userId, "repeatTrNovel");
            if (repeatTrNovel.isEmpty()) {
                userOperationLogService.addLog(userId,"repeatTrNovel", String.valueOf(0));
            }
            int repeatTrNovelNum = Integer.parseInt(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("repeatTrNovelNum").getValueField());
            List<UserOperationLog> repeatTrNovel1 = userOperationLogService.getTodayLogs(userId, "repeatTrNovel");
            UserOperationLog userOperationLog = repeatTrNovel1.get(0);
            if (Integer.parseInt(userOperationLog.getContent()) > repeatTrNovelNum) {
                return "今天重新翻译的次数到达上限：" + repeatTrNovelNum;
            } else {
                userOperationLog.setContent(String.valueOf(Integer.parseInt(userOperationLog.getContent()) + 1));
                userOperationLogRepository.save(userOperationLog);
            }

            Novel novel = novelRepository.getReferenceById(novelId);
            novel.setFontNumber(0L);
            chapterExecuteRepository.deleteAllByNovelId(novelId);
            terminologyRepository.deleteByNovelId(novelId);
            chapterRepository.deleteAllByNovelId(novelId);
            shuTuNovelChapterRepository.deleteByNovelId(novelId);
            // 只有在扩展数据库可用时才删除
            if (chapterScalingUpOneRepository != null) {
                chapterScalingUpOneRepository.deleteByNovelId(novelId);
            }
            novelRepository.save(novel);
            return "处理成功";
        }catch (Exception e) {
            e.printStackTrace();
            return "清理失败";
        }
    }

    @Transactional
    public String deleteBook(Long novelId, Long userId) {
        Optional<UserNovelRelation> byUserIdAndNovelId = userNovelRelationRepository.findByUserIdAndNovelId(userId, novelId);
        if (byUserIdAndNovelId.isEmpty()) {
            return "本书不存在";
        } else {
            UserNovelRelation userNovelRelation = byUserIdAndNovelId.get();
            Long novelId1 = userNovelRelation.getNovelId();
            novelRepository.deleteById(novelId1);
            chapterRepository.deleteAllByNovelId(novelId1);
            chapterExecuteRepository.deleteAllByNovelId(novelId1);
            // 只有在扩展数据库可用时才删除
            if (chapterScalingUpOneRepository != null) {
                chapterScalingUpOneRepository.deleteByNovelId(novelId);
            }
            return "删除成功";
        }
    }


    @Transactional
    public String addTag(DiffDto diffDto, User userByToken) {
        userOperationLogService.addLog(userByToken.getId(),"addTag",diffDto.toString());
        Long novelId = diffDto.getNovelId();
        Optional<Novel> byId = novelRepository.findById(novelId);
        if (byId.isEmpty() || byId.get().getPlatform().equals("novelPia")) {
            return "不允许修改np站的标签";
        }
        List<String> add = diffDto.getAdd();
        List<Long> delete = diffDto.getDelete();
        List<Tag> tagList1 = tagRepository.findByPlatformAndIdIn("user", delete);
        List<Tag> tagList2 = tagRepository.findByPlatformAndIdIn("upload", delete);
        List<Tag> tagList3 = tagRepository.findByPlatformAndIdIn("shutu", delete);
        List<Long> list1 = tagList1.stream().map(Tag::getId).toList();
        List<Long> list2 = tagList2.stream().map(Tag::getId).toList();
        List<Long> list3 = tagList3.stream().map(Tag::getId).toList();
        List<Long> all = new ArrayList<>();
        all.addAll(list2);
        all.addAll(list1);
        all.addAll(list3);
        novelTagRepository.deleteByNovelIdAndTagIdIn(diffDto.getNovelId(),all);
        for (String s : add) {
            Tag tagToSave = new Tag(s, "user", s);
            Optional<Tag> existingTag = tagRepository.findByPlatformAndTrueName("user",tagToSave.getTrueName());
            if (existingTag.isEmpty()) {
                Tag syosetuTag = tagRepository.save(tagToSave);
                novelTagRepository.save(new NovelTag(diffDto.getNovelId(), syosetuTag.getId()));
            } else {
                Tag tag1 = existingTag.get();
                novelTagRepository.save(new NovelTag(diffDto.getNovelId(), tag1.getId()));
            }
        }
        return "执行完成";
    }
}