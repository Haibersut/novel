package com.wtl.novel.Service;

import com.wtl.novel.CDO.ChapterRequest;
import com.wtl.novel.CDO.UserTerminologyModifyCTO;
import com.wtl.novel.entity.UserGlossary;
import com.wtl.novel.repository.UserGlossaryRepository;
import com.wtl.novel.util.JsonEscapeUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class UserGlossaryService {
    private final UserGlossaryRepository userGlossaryRepository;
    private final ConcurrentHashMap<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public UserGlossaryService(UserGlossaryRepository repository) {
        this.userGlossaryRepository = repository;
    }

    /* 1. 根据小说查询全部术语 */
    public List<UserGlossary> listByNovelId(Long novelId) {
        return userGlossaryRepository.findAllByNovelId(novelId);
    }

    /* 2. 批量新增/覆盖 */
    @Transactional
    public UserGlossary batchSave(ChapterRequest cto, Long userId, Long novelId) {
        Optional<UserGlossary> exists = userGlossaryRepository.findByNovelIdAndSourceName(novelId, cto.getTitle());

        if (exists.isPresent()) {
            UserGlossary g = exists.get();
            g.setStatue(100);
            return g;
        }

        UserGlossary newOne = new UserGlossary();
        newOne.setUserId(userId);
        newOne.setNovelId(novelId);
        newOne.setSourceName(cto.getTitle());
        newOne.setTargetName(cto.getContent());
        return userGlossaryRepository.save(newOne);
    }

    /* 3. 批量删除（主键列表） */
    @Transactional
    public void batchDelete(List<Long> ids) {
        userGlossaryRepository.deleteAllByIdInBatch(ids);
    }

//    ====

    public Page<UserGlossary> findAllByNovelIdAndStatue(Long novelId, Pageable pageable) {
        return userGlossaryRepository.findAllByNovelId(novelId, pageable);
    }

    public Page<UserGlossary> findByNovelIdAndKeyword(Long novelId, Pageable pageable, String keyword) {
        return userGlossaryRepository.findByNovelIdAndKeyword(novelId,keyword, pageable);
    }

    @Transactional
    public boolean processTerminology(UserTerminologyModifyCTO cto, Long userId) {
        Long novelId = cto.getNovelId();
        try {
            System.out.println("userId:" +userId +"__" + JsonEscapeUtils.terminologyModifyToJson(cto));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ReentrantLock lock = lockMap.computeIfAbsent(novelId, k -> new ReentrantLock());
        if (!lock.tryLock()) {
            throw new RuntimeException("该小说正在处理中，请稍后再试");
        }
        try {
            /* 1. 批量删除 */
            List<Long> deleteIds = cto.getPaginatedToDeleteTerms();
            if (!deleteIds.isEmpty()) {
                userGlossaryRepository.deleteAllByNovelIdAndIdIn(novelId, deleteIds);
            }

            /* 2. 批量修改（若 targetName 为空则直接删，否则只改字段） */
            List<UserGlossary> modifyList = cto.getPaginatedToModifyTerms();
            if (!modifyList.isEmpty()) {
                List<Long> modifyIds = modifyList.stream()
                        .map(UserGlossary::getId)
                        .toList();

                // 一次查出待改行
                List<UserGlossary> dbList = userGlossaryRepository.findAllByNovelIdAndIdIn(novelId, modifyIds);

                // 需要删除的 ID
                Set<Long> toDeleteIds = dbList.stream()
                        .filter(t -> StringUtils.isBlank(t.getTargetName()))
                        .map(UserGlossary::getId)
                        .collect(Collectors.toSet());

                if (!toDeleteIds.isEmpty()) {
                    userGlossaryRepository.deleteAllByNovelIdAndIdIn(novelId, toDeleteIds);
                }

                // 剩余行只做 targetName 更新
                dbList.stream()
                        .filter(t -> !toDeleteIds.contains(t.getId()))
                        .forEach(t -> {
                            // 从请求里拿到新值
                            String newTarget = modifyList.stream()
                                    .filter(r -> r.getId().equals(t.getId()))
                                    .findFirst()
                                    .map(UserGlossary::getModifyName)
                                    .orElse(null);
                            t.setTargetName(newTarget);
                        });

                userGlossaryRepository.saveAll(dbList);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }
}
