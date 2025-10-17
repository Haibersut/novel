package com.wtl.novel.repository;

import com.wtl.novel.DTO.ChapterSimpleInfo;
import com.wtl.novel.entity.ChapterErrorExecute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChapterErrorExecuteRepository extends JpaRepository<ChapterErrorExecute, Long> {
    // 根据 novel_id 获取所有未被删除的章节的 title 字段列表
    @Query("SELECT c.title FROM ChapterErrorExecute c WHERE c.novelId = :novelId AND c.isDeleted = false")
    List<String> findTitlesByNovelIdAndIsDeletedFalse(@Param("novelId") Long novelId);

    // 根据 novel_id 和 chapter_number 查找 ChapterErrorExecute 实体
    ChapterErrorExecute findByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber);

    // 自定义查询返回 id
    @Query("SELECT c.id FROM ChapterErrorExecute c WHERE c.novelId = :novelId AND c.chapterNumber = :chapterNumber AND c.isDeleted = false")
    Long findIdByNovelIdAndChapterNumberAndIsDeletedFalse(@Param("novelId") Long novelId, @Param("chapterNumber") Integer chapterNumber);

    // 获取所有未被删除的章节
    List<ChapterErrorExecute> findAllByIsDeletedFalse();

    // 根据 novel_id 获取所有未被删除的章节
    List<ChapterErrorExecute> findAllByNovelIdAndIsDeletedFalse(Long novelId);

    @Query("SELECT c.title AS title, c.chapterNumber AS chapterNumber " +
            "FROM ChapterErrorExecute c " +
            "WHERE c.novelId = :novelId " +
            "AND c.isDeleted = false " +
            "AND c.nowState <> 2")
    List<ChapterSimpleInfo> findTitlesAndChapterNumbersByNovelId(
            @Param("novelId") Long novelId);

    List<ChapterErrorExecute> findAllByNowStateAndIsDeletedFalse(Integer nowState);

    /**
     * 根据小说ID列表、当前状态和未删除条件查询章节
     */
    @Query("SELECT c FROM ChapterErrorExecute c " +
            "WHERE c.novelId IN :novelIds " +
            "AND c.nowState = :nowState " +
            "AND c.isDeleted = false")
    List<ChapterErrorExecute> findByNovelIdsAndNowStateAndIsDeletedFalse(
            @Param("novelIds") List<Long> novelIds,
            @Param("nowState") Integer nowState);

    @Query("SELECT c.chapterNumber FROM ChapterErrorExecute c WHERE c.novelId = :novelId AND c.isDeleted = false")
    List<Integer> findChapterNumbersByNovelIdAndIsDeletedFalse(@Param("novelId") Long novelId);

    // 自定义方法：更新 now_state=3 的记录为 0
    @Transactional
    @Modifying
    @Query("UPDATE ChapterErrorExecute ce SET ce.nowState = 0 WHERE ce.nowState = 3")
    void updateNowStateToZero();

    // 根据 ID 逻辑删除（设置 is_deleted = true）
    @Modifying
    @Transactional
    @Query("UPDATE ChapterErrorExecute c SET c.isDeleted = true WHERE c.id = :id")
    void softDeleteById(@Param("id") Long id);

}