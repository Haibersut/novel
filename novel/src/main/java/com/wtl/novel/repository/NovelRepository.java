package com.wtl.novel.repository;

import com.wtl.novel.CDO.NovelCTO;
import com.wtl.novel.DTO.NovelSimpleDto;
import com.wtl.novel.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public interface NovelRepository extends JpaRepository<Novel, Long> {

    Page<Novel> findByPlatformAndTitleContainingOrPlatformAndTrueNameContainingOrderByCreatedAtDesc(
            String platform1, String titleKeyword,
            String platform2, String trueNameKeyword,
            Pageable pageable);

    // 根据 platform 分页获取全部小说（按创建时间倒序）
    Page<Novel> findByPlatformOrderByCreatedAtDesc(String platform, Pageable pageable);

//    @Query("SELECT new com.wtl.novel.DTO.NovelSimpleDto(n.id, n.title, n.trueId) FROM Novel n WHERE n.platform = 'novelPia' n.up > 0")
//    List<NovelSimpleDto> findAllByUpGreaterThanZero();

    Page<Novel> findByTitleContaining(String title, Pageable pageable);
    Page<Novel> findByTitleContainingAndIsDeletedFalseOrTrueNameContainingAndIsDeletedFalseOrderByUpDesc(String title, String trueName, Pageable pageable);
    Page<Novel> findAllByIsDeletedFalse(Pageable pageable);
    Novel findByIdAndIsDeletedFalse(Long id);
    List<Novel> findByIdInAndIsDeletedFalse(List<Long> ids);

    Page<Novel> findAllByIsDeletedFalseOrderByUpDesc(Pageable pageable);
    Page<Novel> findByIdInAndIsDeletedFalseOrderByUpDesc(List<Long> ids, Pageable pageable);

    List<Novel> findByIdInAndPlatformEqualsAndUpGreaterThanAndIsDeletedFalseOrderByUpDesc(List<Long> ids, String platform, Integer up);
    List<Novel> findByIdInAndPlatformEqualsAndIsDeletedFalse(List<Long> ids, String platform);
    List<Novel> findByPlatformEqualsAndUpGreaterThanAndIsDeletedFalseOrderByUpDesc(String platform, Integer up);
    // 根据 Pageable 获取数据
    // 根据 Pageable 获取没有被逻辑删除的 Novel 数据
    @Query("SELECT n FROM Novel n WHERE n.isDeleted = false")
    Page<Novel> findAllNotDeletedWithPageable(Pageable pageable);
    // 修改后的两个方法，显式定义查询和排序
    @Query("SELECT n FROM Novel n WHERE n.platform = :platform AND n.novelType = :novelType AND n.fontNumber BETWEEN :fontNumber1 AND :fontNumber2 AND n.isDeleted = false ORDER BY n.up DESC")
    Page<Novel> findByPlatformAndNovelTypeAndFontNumberBetweenAndIsDeletedFalseOrderByUpDesc(
            @Param("platform") String platform,
            @Param("novelType") String novelType,
            @Param("fontNumber1") Long fontNumber1,
            @Param("fontNumber2") Long fontNumber2,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("UPDATE Novel n SET n.title = :title WHERE n.id = :id")
    int updateTitleById(@Param("id") Long id, @Param("title") String title);

    @Query("SELECT n FROM Novel n WHERE n.platform = :platform AND n.fontNumber BETWEEN :fontNumber1 AND :fontNumber2 AND n.isDeleted = false ORDER BY n.up DESC")
    Page<Novel> findByPlatformAndFontNumberBetweenAndIsDeletedFalseOrderByUpDesc(
            @Param("platform") String platform,
            @Param("fontNumber1") Long fontNumber1,
            @Param("fontNumber2") Long fontNumber2,
            Pageable pageable
    );

    @Query("SELECT new com.wtl.novel.CDO.NovelCTO(n, null, null) " +
            "FROM Novel n " +
            "WHERE n.platform = :platform " +
            "AND n.fontNumber BETWEEN :fontNumber1 AND :fontNumber2 " +
            "AND n.isDeleted = false")
    Page<NovelCTO> findByNovelCTOPlatformAndFontNumberBetweenAndIsDeletedFalse(
            @Param("platform") String platform,
            @Param("fontNumber1") Long fontNumber1,
            @Param("fontNumber2") Long fontNumber2,
            Pageable pageable  // 排序信息通过pageable.getSort()动态传入
    );



    @Query("SELECT n FROM Novel n WHERE n.id IN :novelIds AND n.platform = :platform AND n.fontNumber BETWEEN :fontNumber1 AND :fontNumber2 AND n.isDeleted = false ORDER BY n.up DESC")
    Page<Novel> findByNovelIdsAndPlatformAndFontNumberRange(
            @Param("novelIds") List<Long> novelIds,
            @Param("platform") String platform,
            @Param("fontNumber1") Long fontNumber1,
            @Param("fontNumber2") Long fontNumber2,
            Pageable pageable
    );

    @Query("select new com.wtl.novel.CDO.NovelCTO(n, null, null) " +
            "from Novel n where n.authorId = :authorId and n.isDeleted = false")
    List<NovelCTO> findByAuthorIdAndIsDeletedFalse(@Param("authorId") String authorId);


    @Query("select new com.wtl.novel.CDO.NovelCTO(n, null, null) " +
            "from Novel n where n.authorName = :authorName and n.isDeleted = false")
    List<NovelCTO> findByAuthorNameAndIsDeletedFalse(@Param("authorName") String authorName);

    @Query("SELECT new com.wtl.novel.CDO.NovelCTO(n, null, null) " +
            "FROM Novel n " +
            "WHERE n.id IN :novelIds " +
            "AND n.platform = :platform " +
            "AND n.fontNumber BETWEEN :fontNumber1 AND :fontNumber2 " +
            "AND n.isDeleted = false")
    Page<NovelCTO> findNovelCTOByNovelIdsAndPlatformAndFontNumberRange(
            @Param("novelIds") List<Long> novelIds,
            @Param("platform") String platform,
            @Param("fontNumber1") Long fontNumber1,
            @Param("fontNumber2") Long fontNumber2,
            Pageable pageable
    );

    @Query("SELECT n.id FROM Novel n WHERE n.isDeleted = false AND n.up > 0")
    List<Long> findIdsByUpGreaterThanZeroAndIsDeletedFalse();

    List<Novel> findAllByTrueId(String trueId);

    @Modifying
    @Transactional
    @Query("UPDATE Novel n SET n.fontNumber = n.fontNumber + :increment WHERE n.id = :id")
    int incrementFontNumberById(@Param("id") Long id, @Param("increment") Long increment);

    List<Novel> findByIdGreaterThan(Long minId);

    List<Novel> findAllByIdIn(Collection<Long> ids);

    Collection<Long> id(Long id);

    @Query(value = "SELECT * FROM novel WHERE is_deleted = 0 ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Novel findRandomNovel();

    @Query("select n from Novel n where n.trueId in :trueIds")
    List<Novel> findAllByTrueIdIn(@Param("trueIds") Collection<String> trueIds);
}