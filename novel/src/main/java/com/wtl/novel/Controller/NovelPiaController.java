package com.wtl.novel.Controller;

import com.wtl.novel.CDO.NovelActiveMap;
import com.wtl.novel.CDO.NpNovel;
import com.wtl.novel.CDO.SyosetuNovelDetail;
import com.wtl.novel.Service.*;
import com.wtl.novel.entity.*;
import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.repository.NovelDownloadLimitRepository;
import com.wtl.novel.repository.NovelRepository;
import com.wtl.novel.translator.Novelpia;
import com.wtl.novel.translator.Syosetu;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/api/novelPia")
public class NovelPiaController {

    private final ConcurrentHashMap<String, Lock> keywordLocks = new ConcurrentHashMap<>();


    @Autowired
    private Novelpia novelpia;

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
    @Autowired
    private NovelDownloadLimitRepository novelDownloadLimitRepository;


    @GetMapping("/search/{keyword}")
    public String getNovelsByTagId(@PathVariable String keyword) {
        return novelpia.searchWeb(keyword);
    }

    @GetMapping("/getNpPlatform")
    public List<NpPlatform> getNpPlatform() {
        List<NpPlatform> npPlatform = novelpia.getNpPlatform();
        Filter.cleanConfig(npPlatform);
        return npPlatform;
    }

    @PostMapping("/getNpPlatformPage")
    public NpNovel getNpPlatformPage(@RequestBody NovelActiveMap novelActiveMap) {
        return novelpia.getNpPlatformPage(novelActiveMap);
    }

    @GetMapping("/executeDownloadOne/{novelId}")
    public String executeDownloadOne(@PathVariable Long novelId) {
        System.out.println(novelId);

        NovelDownloadLimit downloadLimit = novelDownloadLimitRepository.findById(novelId).orElse(null);
        if (downloadLimit != null) {
            LocalDateTime lastDownloadTime = downloadLimit.getLastDownloadTime();
            long minutesDifference = java.time.temporal.ChronoUnit.MINUTES.between(lastDownloadTime, LocalDateTime.now());
            if (minutesDifference < 2) {
                return "已开启流程";
            }
        }

        novelpia.executeDownloadOne(novelId);

        // 更新最后下载时间
        NovelDownloadLimit newDownloadLimit = new NovelDownloadLimit();
        newDownloadLimit.setNovelId(novelId);
        newDownloadLimit.setLastDownloadTime(LocalDateTime.now());
        novelDownloadLimitRepository.save(newDownloadLimit);

        return "已开启流程";
    }

//    @GetMapping("/getNovelDetail/{novelId}")
//    public String getNovelDetail(@PathVariable Long novelId) {
//        System.out.println(novelId);
//
//        NovelDownloadLimit downloadLimit = novelDownloadLimitRepository.findById(novelId).orElse(null);
//        if (downloadLimit != null) {
//            LocalDateTime lastDownloadTime = downloadLimit.getLastDownloadTime();
//            long minutesDifference = java.time.temporal.ChronoUnit.MINUTES.between(lastDownloadTime, LocalDateTime.now());
//            if (minutesDifference < 2) {
//                return "已开启流程";
//            }
//        }
//
//        novelpia.getNovelDetail(novelId);
//
//        // 更新最后下载时间
//        NovelDownloadLimit newDownloadLimit = new NovelDownloadLimit();
//        newDownloadLimit.setNovelId(novelId);
//        newDownloadLimit.setLastDownloadTime(LocalDateTime.now());
//        novelDownloadLimitRepository.save(newDownloadLimit);
//
//        return "已开启流程";
//    }
//
//    @GetMapping("/executeTask2")
//    public String executeTask2() {
//        novelpia.executeTask2();
//        return "syosetu.searchWeb(keyword)";
//    }
//
//    @GetMapping("/executeTask3")
//    public String executeTask3() {
//        novelpia.executeTask3();
//        return "syosetu.searchWeb(keyword)";
//    }

//    @GetMapping("/executeTask5")
//    public String executeTask5() {
//        novelpia.executeTask5();
//        return "syosetu.searchWeb(keyword)";
//    }
//
//    @GetMapping("/executeTask6")
//    public String executeTask6() {
//        novelpia.fixErrorChapter();
//        return "syosetu.searchWeb(keyword)";
//    }
//    @GetMapping("/executeTask7")
//    public String executeTask7() {
//        novelpia.photo();
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
            List<Novel> allByTrueId = novelRepository.findAllByTrueId(keyword);
            if (!allByTrueId.isEmpty()) {
                return "该小说已经加入汉化进程（或已经汉化过）：{" + allByTrueId.get(0).getId() + "}";
            }
            if (userService.deductPoints(credential.getUser().getId(), deductPoint)) {
                SyosetuNovelDetail syosetuNovelDetail;
                try {

                    syosetuNovelDetail = novelpia.saveNovelDetail(keyword);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                Novel novel = new Novel();
                novel.setTitle(syosetuNovelDetail.getTitle());
                novel.setTrueName(syosetuNovelDetail.getTitle());
                novel.setTrueId(syosetuNovelDetail.getTrueId());
                novel.setPlatform("novelPia");
                novel.setUp(0L);
                novel.setNovelLike(0L);
                novel.setNovelRead(0L);
                novel.setRecommend(0L);
                novel.setFontNumber(0L);
                novel.setAuthorName("");
                novel.setAuthorId("");
                Novel save = novelRepository.save(novel);

                // 添加收藏
                novelService.increaseUp(save.getId(), credential.getUser(), "up", "novelPia", 0L);

                // 添加汉化记录
                UserNovelRelation relation = new UserNovelRelation();
                relation.setUserId(credential.getUser().getId());
                relation.setNovelId(save.getId());
                relation.setPlatform("novelPia");
                userNovelRelationService.saveUserNovelRelation(relation);

                asyncService.saveNovelPiaAsync(keyword, syosetuNovelDetail, save);
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


//    @GetMapping("/saveNovelsByTagIdFix")
//    public String saveNovelsByTagIdFix() {
//        Optional<Novel> byId = novelRepository.findById(351166L);
//        Novel novel1 = byId.get();
////        List<Novel> allByTrueId = novelRepository.findByIdGreaterThan(351166L);
//        List<Novel> allByTrueId = new ArrayList<>();
//        allByTrueId.add(novel1);
//        allByTrueId.forEach(novel -> {
//            try {
//                SyosetuNovelDetail syosetuNovelDetail = novelpia.saveNovelDetail(novel.getTrueId());
//                novel.setTitle(syosetuNovelDetail.getTitle());
//                novel.setTrueName(syosetuNovelDetail.getTitle());
//                Novel save = novelRepository.save(novel);
//                novelpia.saveNovelFix(syosetuNovelDetail, save);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//            ;
//        });
//        return "syosetu.searchWeb(keyword)";
//
//    }
}
