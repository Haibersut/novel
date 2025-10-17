package com.wtl.novel.Service;

import com.wtl.novel.entity.UserOperationLog;
import com.wtl.novel.repository.UserOperationLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserOperationLogService {

    private final UserOperationLogRepository repository;


    public List<UserOperationLog> getTodayLogs(Long userId, String action) {
        LocalDate today = LocalDate.now();          // 2025-10-06
        LocalDateTime start = today.atStartOfDay(); // 2025-10-06 00:00:00
        LocalDateTime end = today.plusDays(1).atStartOfDay(); // 2025-10-07 00:00:00
        return repository.findByUserIdAndActionAndCreatedAtBetween(userId, action, start, end);
    }

    public UserOperationLogService(UserOperationLogRepository repository) {
        this.repository = repository;
    }

    /**
     * 添加一条操作记录
     */
    public UserOperationLog addLog(Long userId, String action, String content) {
        UserOperationLog log = new UserOperationLog(userId, action, content);
        log.setCreatedAt(LocalDateTime.now());
        return repository.save(log);
    }
}