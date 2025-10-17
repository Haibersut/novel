package com.wtl.novel.repository;

import com.wtl.novel.DTO.SourceTargetProjection;
import com.wtl.novel.entity.UserGlossary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserGlossaryRepository extends JpaRepository<UserGlossary, Long> {
    List<SourceTargetProjection> findByNovelId(Long novelId);
    List<UserGlossary> findAllByNovelIdAndIdIn(Long novelId, Collection<Long> ids);
    List<UserGlossary> findAllByNovelId(Long novelId);
    Page<UserGlossary> findAllByNovelId(Long novelId, Pageable pageable);
    void deleteAllByNovelIdAndIdIn(Long novelId, Collection<Long> ids);
    @Query(value = "SELECT t FROM UserGlossary t WHERE t.novelId = :novelId AND (t.sourceName LIKE %:keyword% OR t.targetName LIKE %:keyword%)")
    Page<UserGlossary> findByNovelIdAndKeyword(
            @Param("novelId") Long novelId,
            @Param("keyword") String keyword,
            Pageable pageable);
    Optional<UserGlossary> findByNovelIdAndSourceName(Long novelId, String sourceName);
}