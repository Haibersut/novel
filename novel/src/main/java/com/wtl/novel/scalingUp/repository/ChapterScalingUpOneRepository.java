package com.wtl.novel.scalingUp.repository;


import com.wtl.novel.scalingUp.entity.ChapterScalingUpOne;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
public interface ChapterScalingUpOneRepository extends JpaRepository<ChapterScalingUpOne, Long> {
    @Transactional
    @Modifying
    @Query("DELETE FROM ChapterScalingUpOne c WHERE c.novelId = :novelId")
    void deleteByNovelId(@Param("novelId") Long novelId);
}