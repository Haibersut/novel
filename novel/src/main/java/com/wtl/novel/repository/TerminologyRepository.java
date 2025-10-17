package com.wtl.novel.repository;

import com.wtl.novel.entity.Terminology;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface TerminologyRepository extends JpaRepository<Terminology, Long> {
//    List<Terminology> findAllByNovelId(Long novelId);

    /**
     * 根据novelId删除所有术语（使用JPQL）
     */
    @Modifying
    @Query("DELETE FROM Terminology t WHERE t.novelId = :novelId")
    void deleteByNovelId(@Param("novelId") Long novelId);

    @Query(value = "SELECT t FROM Terminology t WHERE t.novelId = :novelId AND NOT (t.sourceName = '已下载' AND t.targetName = '已下载')")
    List<Terminology> findAllByNovelId(@Param("novelId") Long novelId);
    Page<Terminology> findAllByNovelId(Long novelId, Pageable pageable);
    @Query(value = "SELECT DISTINCT t.chapterNum FROM Terminology t WHERE t.novelId = :novelId")
    Set<Integer> findDistinctChapterNumbersByNovelId(@Param("novelId") Long novelId);

    // 根据 chapter_true_id 列表查询数据，并返回去重后的 chapter_true_id
    @Query(value = "SELECT DISTINCT t.chapterTrueId FROM Terminology t WHERE t.chapterTrueId IN :chapterTrueIds")
    Set<String> findDistinctChapterTrueIdsByChapterTrueIdIn(@Param("chapterTrueIds") List<String> chapterTrueIds);

    // 根据 novel_id 分页查询
//    Page<Terminology> findByNovelId(Long novelId, Pageable pageable);

    @Query(value = "SELECT t FROM Terminology t WHERE t.novelId = :novelId AND NOT (t.sourceName = '已下载' AND t.targetName = '已下载')")
    Page<Terminology> findByNovelId(@Param("novelId") Long novelId, Pageable pageable);

    @Query(value = "SELECT DISTINCT t.chapterNum FROM Terminology t WHERE t.novelId = :novelId AND t.sourceName = :sourceName AND t.targetName = :targetName")
    Set<Integer> findDistinctChapterNumbersByNovelIdAndSourceTargetDownloaded(
            @Param("novelId") Long novelId,
            @Param("sourceName") String sourceName,
            @Param("targetName") String targetName);

    List<Terminology> findAllByNovelIdAndIdIn(Long novelId, Collection<Long> ids);
    void deleteAllByNovelIdAndIdIn(Long novelId, Collection<Long> ids);

    @Query(value = "SELECT t FROM Terminology t WHERE t.novelId = :novelId AND (t.sourceName LIKE %:keyword% OR t.targetName LIKE %:keyword%)")
    Page<Terminology> findByNovelIdAndKeyword(
            @Param("novelId") Long novelId,
            @Param("keyword") String keyword,
            Pageable pageable);
}