package com.wtl.novel.repository;


import com.wtl.novel.DTO.ChapterProjection;
import com.wtl.novel.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    @Modifying
    @Query("DELETE FROM Chapter c WHERE c.novelId = :novelId")
    void deleteAllByNovelId(@Param("novelId") Long novelId);
    Chapter findByIdAndIsDeletedFalse(Long id);
    Page<ChapterProjection> findAllByNovelIdAndIsDeletedFalse(Long novelId, Pageable pageable);
    List<ChapterProjection> findAllByNovelIdAndIsDeletedFalse(Long novelId);
    // 根据novelId和chapterNumber查找Chapter实体
    Chapter findByNovelIdAndChapterNumberAndIsDeletedFalse(Long novelId, Integer chapterNumber);
    // 自定义查询返回 id
    @Query("SELECT c.id FROM Chapter c " +
            "WHERE c.novelId = :novelId " +
            "AND c.chapterNumber = :chapterNumber " +
            "AND c.isDeleted = false")
    Long findIdByNovelIdAndChapterNumberAndIsDeletedFalse(
            @Param("novelId") Long novelId,
            @Param("chapterNumber") Integer chapterNumber
    );

    // 根据 novelId 获取所有未被删除的章节的 title 字段列表
    @Query("SELECT c.title FROM Chapter c WHERE c.novelId = :novelId AND c.isDeleted = false")
    List<String> findTitlesByNovelIdAndIsDeletedFalse(@Param("novelId") Long novelId);

    // 根据 novelId 获取所有未被删除的章节的 title 字段列表
    @Query("SELECT c.trueId FROM Chapter c WHERE c.novelId = :novelId AND c.isDeleted = false")
    List<String> findTrueIdsByNovelIdAndIsDeletedFalse(@Param("novelId") Long novelId);

    @Query("SELECT c.id FROM Chapter c WHERE c.novelId = :novelId AND c.isDeleted = false ORDER BY c.chapterNumber ASC")
    List<Long> findIdsByNovelIdAndIsDeletedFalseOrderByChapterNumberAsc(@Param("novelId") Long novelId);

    List<Chapter> findByTrueId(String trueId);

    // 自定义返回类型
    public interface ChapterNumberProjection {
        Long getId();
        Integer getChapterNumber();
    }

    @Query("SELECT c.id as id, c.chapterNumber as chapterNumber " +
            "FROM Chapter c " +
            "WHERE c.id IN :ids AND c.isDeleted = false")
    List<ChapterNumberProjection> findChapterNumbersByIds(@Param("ids") List<Long> ids);

    @Query("SELECT c.chapterNumber FROM Chapter c WHERE c.novelId = :novelId AND c.isDeleted = false ORDER BY c.chapterNumber ASC")
    List<Integer> findChapterNumbersByNovelIdAndIsDeletedFalse(@Param("novelId") Long novelId);

    // 根据 chapterId 列表查询数据
    List<Chapter> findByIdInAndIsDeletedFalse(List<Long> chapterIds);

    @Query("SELECT c.chapterNumber FROM Chapter c WHERE c.id = :id AND c.isDeleted = false")
    Integer findChapterNumberById(@Param("id") Long id);

    @Query(value = "SELECT MAX(c.chapterNumber) FROM Chapter c WHERE c.novelId = :novelId")
    Optional<Integer> findMaxChapterNumberByNovelId(Long novelId);

    @Query("SELECT c.novelId as novelId, COUNT(c) as count " +
            "FROM Chapter c " +
            "WHERE c.novelId IN :novelIds AND c.isDeleted = false " +
            "GROUP BY c.novelId")
    List<Object[]> countChaptersByNovelIds(@Param("novelIds") List<Long> novelIds);

    // 添加一个默认方法将结果转换为Map
    default Map<Long, Integer> getChapterCountsByNovelIds(List<Long> novelIds) {
        List<Object[]> results = countChaptersByNovelIds(novelIds);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],    // novelId
                        result -> ((Long) result[1]).intValue()  // count转为Integer
                ));
    }


    @Query(value = "SELECT * FROM chapter WHERE novel_id = :novelId AND is_deleted = 0 ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Chapter findRandomChapterByNovelId(@Param("novelId") Long novelId);

}