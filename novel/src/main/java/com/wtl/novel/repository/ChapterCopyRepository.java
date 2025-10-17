package com.wtl.novel.repository;


import com.wtl.novel.DTO.ChapterProjection;
import com.wtl.novel.entity.Chapter;
import com.wtl.novel.entity.ChapterCopy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterCopyRepository extends JpaRepository<ChapterCopy, Long> {
}