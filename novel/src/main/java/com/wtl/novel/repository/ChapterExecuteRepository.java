package com.wtl.novel.repository;

import com.wtl.novel.DTO.ChapterSimpleInfo;
import com.wtl.novel.entity.ChapterExecute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ChapterExecuteRepository extends JpaRepository<ChapterExecute, Long> {

    @Modifying
    @Query("DELETE FROM ChapterExecute c WHERE c.novelId = :novelId")
    void deleteAllByNovelId(@Param("novelId") Long novelId);
    // 根据 novel_id 获取所有未被删除的章节的 title 字段列表
    @Query("SELECT c.title FROM ChapterExecute c WHERE c.novelId = :novelId AND c.isDeleted = false")
    List<String> findTitlesByNovelIdAndIsDeletedFalse(@Param("novelId") Long novelId);

    // 根据 novel_id 和 chapter_number 查找 ChapterExecute 实体
    ChapterExecute findByNovelIdAndChapterNumberAndIsDeletedFalse(Long novelId, Integer chapterNumber);

    // 自定义查询返回 id
    @Query("SELECT c.id FROM ChapterExecute c WHERE c.novelId = :novelId AND c.chapterNumber = :chapterNumber AND c.isDeleted = false")
    Long findIdByNovelIdAndChapterNumberAndIsDeletedFalse(@Param("novelId") Long novelId, @Param("chapterNumber") Integer chapterNumber);

    // 获取所有未被删除的章节
    List<ChapterExecute> findAllByIsDeletedFalse();

    // 根据 novel_id 获取所有未被删除的章节
    List<ChapterExecute> findAllByNovelIdAndIsDeletedFalse(Long novelId);

    @Query("SELECT c.title AS title, c.chapterNumber AS chapterNumber " +
            "FROM ChapterExecute c " +
            "WHERE c.novelId = :novelId " +
            "AND c.isDeleted = false " +
            "AND c.nowState <> 2")
    List<ChapterSimpleInfo> findTitlesAndChapterNumbersByNovelId(
            @Param("novelId") Long novelId);

    List<ChapterExecute> findAllByNowStateAndIsDeletedFalse(Integer nowState);

    /**
     * 根据小说ID列表、当前状态和未删除条件查询章节
     */
    @Query("SELECT c FROM ChapterExecute c " +
            "WHERE c.novelId IN :novelIds " +
            "AND c.nowState = :nowState " +
            "AND c.isDeleted = false")
    List<ChapterExecute> findByNovelIdsAndNowStateAndIsDeletedFalse(
            @Param("novelIds") List<Long> novelIds,
            @Param("nowState") Integer nowState);

    @Query("SELECT c.chapterNumber FROM ChapterExecute c WHERE c.novelId = :novelId AND c.isDeleted = false")
    List<Integer> findChapterNumbersByNovelIdAndIsDeletedFalse(@Param("novelId") Long novelId);

    // 自定义方法：更新 now_state=3 的记录为 0
    @Transactional
    @Modifying
    @Query("UPDATE ChapterExecute ce SET ce.nowState = 0 WHERE ce.nowState = 3")
    void updateNowStateToZero();


    // 根据小说ID列表、当前状态和未删除条件查询小说ID集合
    @Query("SELECT DISTINCT c.novelId FROM ChapterExecute c " +
            "WHERE c.novelId IN :novelIds " +
            "AND c.nowState = :nowState " +
            "AND c.isDeleted = false")
    Set<Long> findDistinctNovelIdsByNovelIdsAndNowStateAndIsDeletedFalse(
            @Param("novelIds") List<Long> novelIds,
            @Param("nowState") Integer nowState);


    @Query(value = "SELECT MAX(c.chapterNumber) FROM ChapterExecute c WHERE c.novelId = :novelId")
    Optional<Integer> findMaxChapterNumberByNovelId(Long novelId);
}