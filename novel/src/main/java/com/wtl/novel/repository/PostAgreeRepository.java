package com.wtl.novel.repository;


import com.wtl.novel.entity.PostAgree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PostAgreeRepository extends JpaRepository<PostAgree, Long> {

    Optional<PostAgree> findByPostIdAndUserId(Long postId, Long userId);

    long countByPostIdAndAgreeTrue(Long postId);

    // 如果业务上需要“先删除再插入”或“改状态”两种更新方式，可任选其一：
    // 方式 A：直接更新 agree 字段
    @Transactional
    @Modifying
    @Query("update PostAgree p set p.agree = :agree where p.postId = :postId and p.userId = :userId")
    int updateAgree(Long postId, Long userId, Boolean agree);
}