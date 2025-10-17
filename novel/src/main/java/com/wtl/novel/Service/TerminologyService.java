package com.wtl.novel.Service;

import com.wtl.novel.CDO.TerminologyModifyCTO;
import com.wtl.novel.entity.*;
import com.wtl.novel.repository.ChapterRepository;
import com.wtl.novel.repository.NovelRepository;
import com.wtl.novel.repository.TerminologyRepository;
import com.wtl.novel.repository.UserNovelRelationRepository;
import com.wtl.novel.util.JsonEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class TerminologyService {

    @Autowired
    private TerminologyRepository terminologyRepository;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private ChapterService chapterService;

    @Autowired
    private NovelRepository novelRepository;
    @Autowired
    private UserNovelRelationRepository userNovelRelationRepository;

    public Terminology save(Terminology save) {
        return terminologyRepository.save(save);
    }

    public Page<Terminology> findAllByNovelIdAndStatue(Long novelId, Pageable pageable) {
        return terminologyRepository.findAllByNovelId(novelId, pageable);
    }

    public List<Terminology> findAllByNovelId(Long novelId) {
        List<Terminology> list = terminologyRepository.findAllByNovelId(novelId);
        List<Terminology> allByNovelId = terminologyRepository.findAllByNovelId(0L);
        list.addAll(allByNovelId);
        return list;
    }

    public Set<Integer> findDistinctChapterNumbersByNovelId(Long novelId) {
        Set<Integer> set = new HashSet<>(terminologyRepository.findDistinctChapterNumbersByNovelId(0L));
        set.addAll(terminologyRepository.findDistinctChapterNumbersByNovelId(novelId));
        return set;
    }

    public Set<Integer> findDistinctChapterNumbersByNovelIdAndSourceTargetDownloaded(Long novelId) {
        return terminologyRepository.findDistinctChapterNumbersByNovelIdAndSourceTargetDownloaded(novelId,"已下载","已下载");
    }

    // 根据 chapter_true_id 列表查询数据，并返回去重后的 chapter_true_id
    public Set<String> findDistinctChapterTrueIdsByChapterTrueIdIn(List<String> chapterTrueIds) {
        return terminologyRepository.findDistinctChapterTrueIdsByChapterTrueIdIn(chapterTrueIds);
    }

    // 根据 novel_id 分页查询
    public Page<Terminology> findByNovelId(Long novelId, Pageable pageable) {
        return terminologyRepository.findByNovelId(novelId, pageable);
    }

    public Page<Terminology> getTerminologyByPlatform(Long novelId, Pageable pageable) {
        return terminologyRepository.findByNovelId(novelId, pageable);
    }

    /* 锁缓存：每个 novelId 一把锁 */
    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Transactional
    public boolean processTerminology(TerminologyModifyCTO cto, Long userId) {
        Long novelId = cto.getNovelId();
        try {
            System.out.println("userId:" +userId +"__" + JsonEscapeUtils.terminologyModifyToJson(cto));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 1. 获取或创建锁
        ReentrantLock lock = lockMap.computeIfAbsent(novelId, k -> new ReentrantLock());

        // 2. 非阻塞尝试加锁
        if (!lock.tryLock()) {
            throw new RuntimeException("该小说正在处理中，请稍后再试");
        }

        try {
            /* 4. 删除逻辑 */
            List<Long> deleteIds = cto.getPaginatedToDeleteTerms();
            if (!deleteIds.isEmpty()) {
                terminologyRepository.deleteAllByNovelIdAndIdIn(novelId, deleteIds);
            }

            /* 5. 修改逻辑 */
            List<Terminology> paginatedToModifyTerms = cto.getPaginatedToModifyTerms();


            if (!paginatedToModifyTerms.isEmpty()) {
                List<Long> modifyIds = paginatedToModifyTerms.stream()
                        .map(Terminology::getId)
                        .toList();

                List<Terminology> dbTerms = terminologyRepository.findAllByNovelIdAndIdIn(novelId, modifyIds);

                Set<Long> emptyTargetIds = dbTerms.stream()
                        .filter(t -> "".equals(t.getTargetName()))   // 也可根据需要改成 isBlank/isEmpty
                        .map(Terminology::getId)
                        .collect(Collectors.toSet());
                terminologyRepository.deleteAllByNovelIdAndIdIn(novelId, emptyTargetIds);

                dbTerms.removeIf(t -> emptyTargetIds.contains(t.getId()));

                paginatedToModifyTerms.removeIf(t -> emptyTargetIds.contains(t.getId()));


                Map<Long, Terminology> idToDbTerm = dbTerms.stream()
                        .collect(Collectors.toMap(Terminology::getId, t -> t));

                paginatedToModifyTerms.forEach(reqTerm -> {
                    Terminology dbTerm = idToDbTerm.get(reqTerm.getId());
                    if (dbTerm != null) {
                        reqTerm.setTargetName(dbTerm.getTargetName());
                        dbTerm.setTargetName(reqTerm.getModifyName());
                    }
                });

                Map<String, String> targetToModify = paginatedToModifyTerms.stream()
                        // 1. 先按 key 长度降序
                        .sorted(Comparator.comparingInt((Terminology t) -> t.getTargetName().length()).reversed())
                        // 2. 收集到 LinkedHashMap 保证顺序
                        .collect(Collectors.toMap(
                                Terminology::getTargetName,
                                Terminology::getModifyName,
                                (v1, v2) -> v2,               // 冲突时保留后者
                                LinkedHashMap::new));         // 保持排序


                if (targetToModify.isEmpty()) {
                    return true;
                }

                chapterService.processTerminology(cto,targetToModify);


                terminologyRepository.saveAll(dbTerms);
            }

            return true;
        } finally {
            lock.unlock();   // 保证一定释放
        }
    }

    public Page<Terminology> findByNovelIdAndKeyword(Long novelId, Pageable pageable, String keyword) {
        return terminologyRepository.findByNovelIdAndKeyword(novelId,keyword, pageable);
    }
}