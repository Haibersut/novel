package com.wtl.novel.repository;

import com.wtl.novel.entity.InvitationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvitationCodeRepository extends JpaRepository<InvitationCode, Long> {
    InvitationCode findByCode(String code);
    // 新增方法：根据 userId 和 used 状态查询邀请码

    List<InvitationCode> findByUserIdAndUsed(Long userId, boolean used);

    boolean existsByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Modifying
    @Transactional
    @Query("UPDATE InvitationCode ic SET ic.boundEmail = :newEmail WHERE ic.boundEmail = :oldEmail")
    int updateEmailByOldEmail(@Param("oldEmail") String oldEmail, @Param("newEmail") String newEmail);
}