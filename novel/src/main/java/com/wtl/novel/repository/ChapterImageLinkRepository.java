package com.wtl.novel.repository;

import com.wtl.novel.entity.ChapterImageLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChapterImageLinkRepository extends JpaRepository<ChapterImageLink, Long> {

    // 根据 chapter_true_id 查询记录
    List<ChapterImageLink> findByChapterTrueId(String chapterTrueId);

    @Modifying
    @Query("DELETE FROM ChapterImageLink c WHERE c.chapterTrueId = :chapterTrueId")
    void deleteByChapterTrueId(String chapterTrueId);

    List<ChapterImageLink> findAllByChapterTrueIdIn(Collection<String> chapterTrueIds);
    // 查询 chapterTrueId 在指定集合中且 cf 为 false 的记录
    List<ChapterImageLink> findAllByChapterTrueIdInAndCf(Collection<String> chapterTrueIds, boolean cf);

}