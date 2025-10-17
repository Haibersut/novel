package com.wtl.novel.Controller;


import com.wtl.novel.CDO.SyosetuNovelDetail;
import com.wtl.novel.Service.*;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.Novel;
import com.wtl.novel.entity.UserNovelRelation;
import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.repository.NovelRepository;
import com.wtl.novel.translator.Syosetu;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/api/syosetu")
public class SyosetuController {

    private final ConcurrentHashMap<String, Lock> keywordLocks = new ConcurrentHashMap<>();


    @Autowired
    private Syosetu syosetu;

    @Autowired
    private NovelRepository novelRepository;
    @Autowired
    private AsyncService asyncService;
    @Autowired
    private CredentialService credentialService;
    @Autowired
    private UserService userService;

    @Autowired
    private DictionaryRepository dictionaryRepository;
    @Autowired
    private NovelService novelService;

    @Autowired
    private UserNovelRelationService userNovelRelationService;


    @GetMapping("/search/{keyword}")
    public String getNovelsByTagId(@PathVariable String keyword) {
        return syosetu.searchWeb(keyword);
    }
//
//    @GetMapping("/executeTask1")
//    public String executeTask1() {
//        syosetu.executeTask1();
//        return "syosetu.searchWeb(keyword)";
//    }
//
//    @GetMapping("/executeTask2")
//    public String executeTask2() {
//        syosetu.fixErrorChapter();
//        return "syosetu.searchWeb(keyword)";
//    }

    @GetMapping("/save/{keyword}")
    public String saveNovelsByTagId(@PathVariable String keyword, HttpServletRequest httpRequest) {
        // 获取锁
        Lock lock = keywordLocks.computeIfAbsent(keyword, k -> new ReentrantLock());
        lock.lock();
        try {
            // 检查是否已经有任务在处理该 keyword
            String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
            String authorizationHeader = authorizationInfo[0];
            Credential credential = credentialService.findByToken(authorizationHeader);
            if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
                return "用户未登录！";
            }

            Long deductPoint = Long.valueOf(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("executeSyosetuDeductPoint").getValueField());

            if (userService.deductPoints(credential.getUser().getId(), deductPoint)) {
                SyosetuNovelDetail syosetuNovelDetail;
                try {
                    List<Novel> allByTrueId = novelRepository.findAllByTrueId(keyword);
                    if (!allByTrueId.isEmpty()) {
                        return "该小说已经加入汉化进程（或已经汉化过）：{" + allByTrueId.get(0).getId() + "}";
                    }
                    syosetuNovelDetail = syosetu.saveNovelDetail(keyword);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                Novel novel = new Novel();
                novel.setTitle(syosetuNovelDetail.getTitle());
                novel.setTrueName(syosetuNovelDetail.getTitle());
                novel.setTrueId(syosetuNovelDetail.getTrueId());
                novel.setPlatform("syosetu");
                novel.setUp(0L);
                novel.setFontNumber(0L);
                Novel save = novelRepository.save(novel);

                // 添加收藏
                novelService.increaseUp(save.getId(), credential.getUser(), "up", "syosetu", 0L);

                // 添加汉化记录
                UserNovelRelation relation = new UserNovelRelation();
                relation.setUserId(credential.getUser().getId());
                relation.setNovelId(save.getId());
                userNovelRelationService.saveUserNovelRelation(relation);

                asyncService.saveNovelAsync(keyword, syosetuNovelDetail, save);
                return "已收录本小说，请耐心等待<定时汉化任务>执行，这可能需要几个小时的时间：{" + save.getId() + "}";
            }
            return "积分不足：" + deductPoint + "积分，在社区发布优秀书评，可以获取积分。";
        } finally {
            // 释放锁
            lock.unlock();
            // 如果锁不再被使用，可以移除锁对象以节省内存
            keywordLocks.remove(keyword, lock);
        }
    }
}
