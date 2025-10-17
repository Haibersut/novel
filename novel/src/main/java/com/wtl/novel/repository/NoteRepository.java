package com.wtl.novel.repository;

import com.wtl.novel.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    // 可以添加自定义查询方法
    // 根据用户ID获取笔记
    List<Note> findByUserId(Long userId);

    // 根据小说ID获取笔记
    List<Note> findByNovelId(Long novelId);

    // 根据章节ID获取笔记
    List<Note> findByChapterIdAndIsDeletedFalse(Long chapterId);

    // 根据小说ID和内容模糊匹配获取笔记
    @Query("SELECT n FROM Note n WHERE n.novelId = :novelId AND n.content LIKE %:contentKeyword%")
    List<Note> findByNovelIdAndContentLike(
            @Param("novelId") Long novelId,
            @Param("contentKeyword") String contentKeyword
    );

    @Modifying
    @Transactional
    @Query("UPDATE Note n SET n.isDeleted = true WHERE n.id IN :ids")
    void logicDeleteByIds(@Param("ids") List<Long> ids);

    @Query("SELECT n FROM Note n WHERE n.userId = :userId AND n.chapterId = :chapterId AND n.isDeleted = false")
    List<Note> findByUserIdAndChapterId(
            @Param("userId") Long userId,
            @Param("chapterId") Long chapterId
    );

    @Query("SELECT DISTINCT n.novelId FROM Note n WHERE n.userId = :userId AND n.isDeleted = false")
    List<Long> findNovelIdsByUserId(Long userId);


    @Modifying
    @Transactional
    @Query("DELETE FROM Note n WHERE n.id IN :ids AND n.userId = :userId")
    void deleteByIds(@Param("ids") List<Long> ids,@Param("userId") Long userId);

    // 根据用户ID和小说ID查询未删除的笔记
    List<Note> findByUserIdAndNovelIdAndIsDeletedFalse(Long userId, Long novelId);


}