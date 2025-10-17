package com.wtl.novel.Service;

import com.wtl.novel.entity.Chapter;
import com.wtl.novel.entity.ChapterSync;
import com.wtl.novel.repository.ChapterRepository;
import com.wtl.novel.repository.ChapterSyncRepository;
import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.scalingUp.entity.ChapterScalingUpOne;
import com.wtl.novel.scalingUp.repository.ChapterScalingUpOneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChapterSyncService {

    private static final Logger log = LoggerFactory.getLogger(ChapterSyncService.class);

    @Autowired
    private  ChapterSyncRepository chapterSyncRepository;
    @Autowired(required = false)
    private ChapterScalingUpOneRepository chapterScalingUpOneRepository;
    @Autowired
    private DictionaryRepository dictionaryRepository;
    @Autowired
    private ChapterRepository chapterRepository;

    @Scheduled(fixedDelay = 60_000)
    public void retryUnSyncedChapters() {
        // 如果未配置扩展数据库，直接返回
        if (chapterScalingUpOneRepository == null) {
            return;
        }
        
        try {
            boolean runScalingUp = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("RunScalingUp").getValueField());
            if (!runScalingUp) {
                log.info("已经关闭同步");
                return;
            }
            // 查询未同步的章节
            List<ChapterSync> list = chapterSyncRepository.findUnSyncedAndLock();
            if (list.isEmpty()) {
                log.info("没有发现未同步章节");
                return;
            }

            log.info("发现 {} 条未同步章节，开始重试...", list.size());

            for (ChapterSync cs : list) {
                processSingleChapterWithoutTransaction(cs);
            }
        } catch (Exception e) {
            log.error("查询未同步章节失败", e);
        }
    }

    public void processSingleChapterWithoutTransaction(ChapterSync cs) {
        // 如果未配置扩展数据库，直接返回
        if (chapterScalingUpOneRepository == null) {
            return;
        }
        
        try {
            Chapter chapter = chapterRepository.findById(cs.getChapterId()).orElse(null);
            if (chapter == null) {
                log.warn("chapterId={} 已不存在，跳过", cs.getChapterId());
                return;
            }

            // 方法1：使用 try-catch 包装保存操作
            try {
                ChapterScalingUpOne scalingUpOne = new ChapterScalingUpOne(chapter);
                chapterScalingUpOneRepository.save(scalingUpOne);
                log.info("保存到 scalingUp 库成功，chapterId={}", chapter.getId());
            } catch (Exception e) {
                log.warn("保存到 scalingUp 库失败，尝试检查记录状态，chapterId={}", chapter.getId());
                // 检查记录是否已存在
                if (!chapterScalingUpOneRepository.existsById(chapter.getId())) {
                    throw e;
                }
            }

            // 主库章节内容清空
            chapter.setContent("");
            chapterRepository.save(chapter);

            // 标记同步成功
            cs.setSynced(true);
            chapterSyncRepository.save(cs);

            log.info("章节同步成功，chapterId={}", cs.getChapterId());

        } catch (Exception e) {
            log.error("处理章节失败，进行补偿，chapterId={}", cs.getChapterId(), e);
            compensateChapterSync(cs.getChapterId());
        }
    }

    /**
     * 补偿逻辑：确保数据一致性
     */
    private void compensateChapterSync(Long chapterId) {
        // 如果未配置扩展数据库，直接返回
        if (chapterScalingUpOneRepository == null) {
            return;
        }
        
        try {
            // 1. 检查 scalingUp 库中是否已存在该章节
            boolean existsInScalingUp = chapterScalingUpOneRepository.existsById(chapterId);

            // 2. 如果 scalingUp 库中已存在，但主库标记未同步，需要修正标记
            if (existsInScalingUp) {
                Optional<ChapterSync> syncOpt = chapterSyncRepository.findByChapterId(chapterId);
                if (syncOpt.isPresent()) {
                    ChapterSync cs = syncOpt.get();
                    if (!cs.getSynced()) {
                        cs.setSynced(true);
                        chapterSyncRepository.save(cs);
                        log.info("补偿：修正同步标记，chapterId={}", chapterId);
                    }
                }
            }
            // 3. 如果 scalingUp 库中不存在，保持原状，下次定时任务会重试

        } catch (Exception compEx) {
            log.error("补偿逻辑执行失败，chapterId={}", chapterId, compEx);
        }
    }

    public void save(Chapter chapter) {

        try {
            String ScalingUp = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("ScalingUp").getValueField();
            Optional<ChapterSync> byChapterId = chapterSyncRepository.findByChapterId(chapter.getId());
            if (byChapterId.isEmpty()) {
                ChapterSync chapterSync = new ChapterSync();
                chapterSync.setChapterId(chapter.getId());
                chapterSync.setHostServerName(ScalingUp);
                chapterSync.setSynced(false);
                chapterSyncRepository.save(chapterSync);
            } else {
                ChapterSync chapterSync1 = byChapterId.get();
                chapterSync1.setSynced(false);
                chapterSyncRepository.save(chapterSync1);
            }

//            ChapterScalingUpOne chapterScalingUpOne = new ChapterScalingUpOne(chapter);
//            chapterScalingUpOneRepository.save(chapterScalingUpOne);
//            chapter.setContent("");
//            chapterRepository.save(chapter);
        }catch (Exception e) {
//            chapterSync.setSynced(false);
//            chapterSyncRepository.save(chapterSync);
            log.error("保存章节同步信息失败", e);
        }
    }

    public void saveAll(List<Chapter> chapterList) {
        try {
            List<ChapterSync> chapterSyncList = new ArrayList<>(chapterList.size());

            // 1. 原有逻辑：组装完整列表
            String ScalingUp = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("ScalingUp").getValueField();
            for (Chapter chapter : chapterList) {
                ChapterSync chapterSync = new ChapterSync();
                chapterSync.setChapterId(chapter.getId());
                chapterSync.setHostServerName(ScalingUp);
                chapterSync.setSynced(false);
                chapterSyncList.add(chapterSync);
            }

            // 2. 一次性查出已存在的主键
            List<Long> chapterIds = chapterList.stream()
                    .map(Chapter::getId)
                    .collect(Collectors.toList());
            Set<Long> existIdSet = chapterSyncRepository.findAllByChapterIdIn(chapterIds)
                    .stream()
                    .map(ChapterSync::getChapterId)
                    .collect(Collectors.toSet());

            // 3. 分成两组：已存在 vs 不存在
            List<ChapterSync> existsList = new ArrayList<>();
            List<ChapterSync> notExistsList = new ArrayList<>();

            for (ChapterSync cs : chapterSyncList) {
                if (existIdSet.contains(cs.getChapterId())) {
                    existsList.add(cs);
                } else {
                    notExistsList.add(cs);
                }
            }

            // 4. 已存在的 synced 强制设为 false
            existsList.forEach(cs -> cs.setSynced(false));

            // 5. 两组一起保存（已存在会被更新，不存在被插入）
            chapterSyncRepository.saveAll(existsList);
            chapterSyncRepository.saveAll(notExistsList);
        }catch (Exception e) {
            log.error("批量保存章节同步信息失败", e);
        }
    }
}