package com.wtl.novel.repository;

import com.wtl.novel.entity.UserAccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserAccessLogRepository extends JpaRepository<UserAccessLog, Long> {
    List<UserAccessLog> findByUserIdAndVisitDate(Long userId, LocalDate date);
}