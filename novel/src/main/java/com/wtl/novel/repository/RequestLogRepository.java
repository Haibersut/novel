package com.wtl.novel.repository;

import com.wtl.novel.entity.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {
    RequestLog findByCredentialIdAndRequestTimeAfter(Long credentialId, LocalDateTime todayStart);
}