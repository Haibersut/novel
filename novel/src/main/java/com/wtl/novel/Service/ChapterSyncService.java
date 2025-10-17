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

    @Scheduled(fixedDelay = 60_000, initialDelay = 120_000)
    public void retryUnSyncedChapters() {
        // 如果未配置扩展数据库,直接返回
        if (chapterScalingUpOneRepository == null) {
            return;
        }
        
        try {
            var dictionary = dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("RunScalingUp");
            if (dictionary == null) {
                log.debug("RunScalingUp 配置项不存在,跳过同步任务");
                return;
            }
            
            boolean runScalingUp = Boolean.parseBoolean(dictionary.getValueField());
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

    /**
     * 处理单个章节的同步
     */
    public void processSingleChapterWithoutTransaction(ChapterSync cs) {
        // 如果未配置扩展数据库，直接返回
        if (chapterScalingUpOneRepository == null) {
            log.debug("从库未配置，跳过章节同步");
            return;
        }
        
        if (cs == null || cs.getChapterId() == null) {
            log.warn("ChapterSync 对象或 chapterId 为 null，跳过处理");
            return;
        }
        
        Long chapterId = cs.getChapterId();
        boolean scalingUpSaved = false;
        
        try {
            Chapter chapter = chapterRepository.findById(chapterId).orElse(null);
            if (chapter == null) {
                log.warn("chapterId={} 已不存在，跳过", chapterId);
                return;
            }
            
            if (!chapterId.equals(chapter.getId())) {
                log.error("章节ID不匹配，expected={}, actual={}", chapterId, chapter.getId());
                return;
            }

            // 保存到从数据库（ScalingUp）
            try {
                ChapterScalingUpOne scalingUpOne = new ChapterScalingUpOne(chapter);
                if (scalingUpOne.getId() == null || !scalingUpOne.getId().equals(chapterId)) {
                    log.error("转换后的章节ID不匹配，expected={}, actual={}", chapterId, scalingUpOne.getId());
                    throw new IllegalStateException("章节转换失败");
                }
                
                chapterScalingUpOneRepository.save(scalingUpOne);
                scalingUpSaved = true;  // 标记成功
                log.info("保存到 scalingUp 库成功，chapterId={}", chapterId);
            } catch (Exception e) {
                log.warn("保存到 scalingUp 库失败，检查是否已存在，chapterId={}", chapterId);
                // 检查记录是否已存在
                if (chapterScalingUpOneRepository.existsById(chapterId)) {
                    scalingUpSaved = true;
                    log.info("scalingUp 库中已存在该章节，chapterId={}", chapterId);
                } else {
                    throw new IllegalStateException("从库保存失败且记录不存在", e);
                }
            }

            // 主库章节内容清空
            try {
                chapter.setContent("");
                chapterRepository.save(chapter);
                log.info("主库章节内容已清空，chapterId={}", chapterId);
            } catch (Exception e) {
                log.error("主库更新失败，需要回滚从库数据，chapterId={}", chapterId, e);
                if (scalingUpSaved) {
                    rollbackScalingUpData(chapterId);
                }
                throw new IllegalStateException("主库更新失败", e);
            }

            // 标记同步成功
            try {
                cs.setSynced(true);
                chapterSyncRepository.save(cs);
                log.info("章节同步完成，chapterId={}", chapterId);
            } catch (Exception e) {
                log.error("更新同步状态失败，chapterId={}", chapterId, e);
                throw new IllegalStateException("同步状态更新失败", e);
            }

        } catch (Exception e) {
            log.error("处理章节失败，chapterId={}，错误: {}", chapterId, e.getMessage(), e);
            compensateChapterSync(chapterId);
        }
    }

    /**
     * 回滚从库数据（当主库操作失败时）
     */
    private void rollbackScalingUpData(Long chapterId) {
        try {
            log.warn("开始回滚从库数据，chapterId={}", chapterId);
            chapterScalingUpOneRepository.deleteById(chapterId);
            log.info("从库数据回滚成功，chapterId={}", chapterId);
        } catch (Exception rollbackEx) {
            log.error("从库数据回滚失败，chapterId={}", chapterId, rollbackEx);
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