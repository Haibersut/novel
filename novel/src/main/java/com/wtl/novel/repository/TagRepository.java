package com.wtl.novel.repository;

import com.wtl.novel.entity.Novel;
import com.wtl.novel.entity.NovelTag;
import com.wtl.novel.entity.Tag;
import org.hibernate.query.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository  extends JpaRepository<Tag, Long> {
    List<Tag> findByIdIn(List<Long> ids);
    // 新增方法：根据 platform 查找数据，并按 id 排序
    List<Tag> findByPlatform(String platform, Sort sort);
    // 新增方法：根据 true_name 查询记录
    Optional<Tag> findByTrueName(String trueName);

    // 新增方法：根据 platform 查询数据，限制查询结果数量为1500条
    @Query("SELECT t FROM Tag t WHERE t.platform = :platform ORDER BY t.id")
    List<Tag> findByPlatformLimit(@Param("platform") String platform, Pageable pageable);


    @Query("SELECT t FROM Tag t WHERE (t.name LIKE %:keyword% OR t.trueName LIKE %:keyword%) ORDER BY t.id")
    List<Tag> allByKeyword(@Param("keyword") String keyword);

    @Query("SELECT t FROM Tag t WHERE t.platform = :platform AND (t.name LIKE %:keyword% OR t.trueName LIKE %:keyword%) ORDER BY t.id")
    List<Tag> findByPlatformAnd(@Param("platform") String platform, @Param("keyword") String keyword);

    List<Tag> findByPlatformAndName(String platform, String name);

    @Query("SELECT t FROM Tag t WHERE t.platform = :platform AND t.trueName IN :names")
    List<Tag> findByPlatformAndTrueNameIn(@Param("platform") String platform, @Param("names") List<String> names);


    // 根据 platform 和 trueName 查询唯一记录（如果允许为空，用 Optional 接住）
    Optional<Tag> findByPlatformAndTrueName(String platform, String trueName);

    List<Tag> findByPlatformAndIdIn(String platform, Collection<Long> ids);
}
