package com.wtl.novel.Controller;

import com.wtl.novel.Service.ChapterService;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.UserFeedbackService;
import com.wtl.novel.Service.UserOperationLogService;
import com.wtl.novel.entity.*;
import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.repository.NovelRepository;
import com.wtl.novel.repository.UserOperationLogRepository;
import com.wtl.novel.util.LockManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@RestController
@RequestMapping("/api/feedback")
public class UserFeedbackController {
    @Autowired
    private UserFeedbackService userFeedbackService;
    @Autowired
    private UserOperationLogRepository userOperationLogRepository;
    @Autowired
    private UserOperationLogService userOperationLogService;
    @Autowired
    private DictionaryRepository dictionaryRepository;
    @Autowired
    private CredentialService credentialService;
    @Autowired
    private NovelRepository novelRepository;




    @PostMapping("/add")
    public ResponseEntity<String> createFeedback(@RequestBody UserFeedback feedback, HttpServletRequest httpRequest) {
        // 内容长度验证
        if(feedback.getContent().length() > 5000) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("内容太长");
        }

        // 认证验证
        String authorization = httpRequest.getHeader("Authorization");
        if (authorization == null || authorization.isEmpty()) {
            return ResponseEntity.status(401).body("未授权");
        }
        String[] authorizationInfo = authorization.split(";");
        if (authorizationInfo.length == 0) {
            return ResponseEntity.status(401).body("未授权");
        }
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(401).body("未授权");
        }
        if (credential.getUser() == null) {
            return ResponseEntity.status(401).body("未授权");
        }

        Long userId = credential.getUser().getId();

        // 为用户ID创建锁对象
        String userLockKey = "FEEDBACK_LOCK_USER_" + userId;
        Object userLock = LockManager.getLock(userLockKey);

        synchronized (userLock) {
            // 检查是否已存在反馈
            List<UserFeedback> byNovelIdAndChapterId = userFeedbackService.findByNovelIdAndChapterIdAndIsDeleteFalse(feedback.getNovelId(), feedback.getChapterId());
            if (!byNovelIdAndChapterId.isEmpty()) {
                return ResponseEntity.ok("已经有人提交");
            }

            // 设置用户ID
            feedback.setUserId(userId);

            // 操作日志检查
            List<UserOperationLog> repeatTrNovel = userOperationLogService.getTodayLogs(userId, "createFeedback");
            if (repeatTrNovel.isEmpty()) {
                userOperationLogService.addLog(userId, "createFeedback", String.valueOf(0));
            }

            int createFeedbackNum = Integer.parseInt(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("createFeedbackNum").getValueField());
            List<UserOperationLog> repeatTrNovel1 = userOperationLogService.getTodayLogs(userId, "createFeedback");
            UserOperationLog userOperationLog = repeatTrNovel1.get(0);

            if (Integer.parseInt(userOperationLog.getContent()) > createFeedbackNum) {
                return ResponseEntity.ok("今天重新翻译的次数到达上限：" + createFeedbackNum);
            } else {
                userOperationLog.setContent(String.valueOf(Integer.parseInt(userOperationLog.getContent()) + 1));
                userOperationLogRepository.save(userOperationLog);
            }

            UserFeedback feedback1 = userFeedbackService.createFeedback(feedback);
            return ResponseEntity.ok("提交成功");
        }
    }


    @GetMapping("/status")
    public Page<UserFeedback> getFeedbacksByStatus(
            @RequestParam Boolean isResolved,
            @RequestParam int page,
            @RequestParam int size) {
        return userFeedbackService.getFeedbacksByStatus(isResolved, page, size);
    }

}