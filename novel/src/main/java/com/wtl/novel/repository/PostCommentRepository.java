package com.wtl.novel.repository;

import com.wtl.novel.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    // 根据postId获取所有评论（包括顶级评论和回复）
    List<PostComment> findByPostId(Long postId);

    // 根据parentId获取所有回复
    List<PostComment> findByParentId(Long parentId);

    void deleteAllByPostId(Long id);


    @Modifying
    @Transactional
    @Query("UPDATE PostComment pc SET pc.username = :newUsername WHERE pc.userId = :userId")
    int updateUsernameByUserIdAndOldUsername(@Param("userId") Long userId,
                                             @Param("newUsername") String newUsername);
}