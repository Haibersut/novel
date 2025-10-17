package com.wtl.novel.repository;

import com.wtl.novel.DTO.TextNumCount;
import com.wtl.novel.entity.ChapterComment;
import com.wtl.novel.entity.Comment;
import com.wtl.novel.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChapterCommentRepository extends JpaRepository<ChapterComment, Long> {

    List<ChapterComment> findAllByChapterIdAndTextNum(Long chapterId, Integer textNum);
    List<ChapterComment> findByParentId(Long parentId);
    @Query("select c.textNum as textNum, count(c) as cnt " +
            "from ChapterComment c " +
            "where c.chapterId = :chapterId " +
            "group by c.textNum")
    List<TextNumCount> countByChapterIdGroupByTextNum(@Param("chapterId") Long chapterId);
    @Modifying
    @Query("update ChapterComment c set c.textNum = :newTextNum " +
            "where c.chapterId = :chapterId and c.textNum in :textNumList")
    int updateTextNumByChapterIdAndTextNumIn(@Param("chapterId") Long chapterId,
                                             @Param("textNumList") List<Integer> textNumList,
                                             @Param("newTextNum") Integer newTextNum);


    // 新增方法：根据用户ID和旧用户名更新用户名（更安全的方式）
    @Modifying
    @Transactional
    @Query("UPDATE ChapterComment cc SET cc.username = :newUsername WHERE cc.userId = :userId")
    int updateUsernameByUserIdAndOldUsername(@Param("userId") Long userId,
                                             @Param("newUsername") String newUsername);

}