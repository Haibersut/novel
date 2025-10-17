package com.wtl.novel.repository;


import com.wtl.novel.entity.FavoriteGroups;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteGroupsRepository extends JpaRepository<FavoriteGroups, Long> {

    // 根据用户ID查询所有分组
    List<FavoriteGroups> findAllByUserId(Long userId);

    // 根据用户ID和分组名称查询分组
    Optional<FavoriteGroups> findByNameAndUserId(String name, Long userId);

    // 根据用户ID删除所有分组
    void deleteAllByUserId(Long userId);
}