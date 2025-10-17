package com.wtl.novel.repository;

import com.wtl.novel.entity.NovelDownloadLimit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NovelDownloadLimitRepository extends JpaRepository<NovelDownloadLimit, Long> {
}