package com.wtl.novel.repository;

import com.wtl.novel.entity.ShuTuNovelChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShuTuNovelChapterRepository extends JpaRepository<ShuTuNovelChapter, Long> {
    Optional<ShuTuNovelChapter> findByNovelIdAndChapterNum(Long novelId, Integer chapterNum);
    List<ShuTuNovelChapter> findByNovelIdAndChapterNumIn(Long novelId, Collection<Integer> chapterNums);
    // 根据 novelId 一次性查出全部章节（按章节号升序）
    List<ShuTuNovelChapter> findAllByNovelIdOrderByChapterNum(Long novelId);
    // 只查询指定 novelId 且未下载的章节，仍按 chapterNum 升序
    List<ShuTuNovelChapter> findAllByNovelIdAndDownloadedTrueOrderByChapterNum(Long novelId);
    @Modifying
    @Query("DELETE FROM ShuTuNovelChapter c WHERE c.novelId = :novelId")
    void deleteByNovelId(@Param("novelId") Long novelId);
}