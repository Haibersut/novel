package com.wtl.novel.repository;

import com.wtl.novel.entity.ChapterSync;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterSyncRepository extends JpaRepository<ChapterSync, Long> {
    Optional<ChapterSync> findByChapterId(Long chapterId);
    List<ChapterSync> findByChapterIdInAndSyncedTrue(List<Long> chapterIds);

    @Query("select cs from ChapterSync cs where cs.synced = false")
    List<ChapterSync> findUnSyncedAndLock();
    List<ChapterSync> findAllByChapterIdIn(Collection<Long> chapterIds);


}