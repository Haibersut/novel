package com.wtl.novel.repository;


import com.wtl.novel.entity.UserFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {
    Page<UserFeedback> findByIsResolved(Boolean isResolved, Pageable pageable);
    List<UserFeedback> findByIsDeleteFalse();
    List<UserFeedback> findByNovelIdAndChapterIdAndIsDeleteFalse(Long novelId, Long chapterId);

    @Modifying
    @Transactional
    @Query("UPDATE UserFeedback u SET u.isDelete = true WHERE u.novelId = :novelId AND u.chapterId = :chapterId")
    void softDeleteByUserAndContent(
            @Param("novelId") Long novelId,
            @Param("chapterId") Long chapterId
    );


}