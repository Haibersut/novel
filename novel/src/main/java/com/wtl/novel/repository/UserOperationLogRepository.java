package com.wtl.novel.repository;

import com.wtl.novel.entity.UserOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserOperationLogRepository extends JpaRepository<UserOperationLog, Long> {
    /* 方法 1：纯命名规则写法（推荐，最简洁） */
    List<UserOperationLog> findByUserIdAndActionAndCreatedAtBetween(
            Long userId,
            String action,
            LocalDateTime startTime,
            LocalDateTime endTime);
}