package com.wtl.novel.repository;

import com.wtl.novel.entity.ReadingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReadingRecordRepository extends JpaRepository<ReadingRecord, Long> {
    // 根据用户ID和小说ID查询阅读记录
    ReadingRecord findByUserIdAndNovelId(Long userId, Long novelId);

    // 根据用户ID和小说ID列表查询阅读记录
    List<ReadingRecord> findByUserIdAndNovelIdIn(Long userId, List<Long> novelIds);

    // 可选：根据用户ID查询所有阅读记录
    List<ReadingRecord> findByUserId(Long userId);

    // 可选：根据小说ID查询所有阅读记录
    List<ReadingRecord> findByNovelId(Long novelId);

    List<ReadingRecord> findTop50ByUserIdOrderByUpdateTimeDesc(Long userId);
}