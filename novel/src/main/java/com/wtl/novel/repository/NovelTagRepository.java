package com.wtl.novel.repository;

import com.wtl.novel.entity.NovelTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NovelTagRepository extends JpaRepository<NovelTag, Long> {
    Page<NovelTag> findById(Long id, Pageable pageable);
    List<NovelTag> findByNovelId(Long novelId);
    List<NovelTag> findByTagId(Long tagId);
    // 如果需要分页并去重，可以添加以下方法
    @Query("SELECT nt.novelId FROM NovelTag nt GROUP BY nt.novelId HAVING SUM(CASE WHEN nt.tagId IN :tagIds THEN 1 ELSE 0 END) = 0")
    List<Long> findDistinctNovelIdByTagIdNotIn(List<Long> tagIds);
    // 根据小说 ID 查询所有 tagId
    @Query("SELECT nt.tagId FROM NovelTag nt WHERE nt.novelId = :novelId")
    List<Long> findTagIdsByNovelId(Long novelId);
    void deleteByNovelId(Long novelId);
    // 根据标签ID列表查询小说ID，确保小说包含所有指定的标签
    @Query("SELECT nt.novelId FROM NovelTag nt WHERE nt.tagId IN :tagIds GROUP BY nt.novelId HAVING COUNT(DISTINCT nt.tagId) = :tagCount")
    List<Long> findNovelIdsByAllTagIds(@Param("tagIds") List<Long> tagIds, @Param("tagCount") int tagCount);

    @Query("SELECT nt FROM NovelTag nt WHERE nt.novelId = :novelId AND nt.tagId = :tagId")
    Optional<NovelTag> findByNovelIdAndTagId(@Param("novelId") Long novelId, @Param("tagId") Long tagId);

    List<NovelTag> findAllByNovelIdIn(Collection<Long> novelIds);
    void deleteByNovelIdAndTagIdIn(Long novelId, Collection<Long> tagIds);

}