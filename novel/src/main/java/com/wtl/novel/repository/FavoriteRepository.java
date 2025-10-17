package com.wtl.novel.repository;

import com.wtl.novel.DTO.UserIdProjection;
import com.wtl.novel.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // 根据用户ID查询所有收藏
    List<Favorite> findByUserId(Long userId);

    // 根据用户ID和收藏类型查询收藏
    List<Favorite> findByUserIdAndFavoriteType(Long userId, String favoriteType);

    // 根据用户ID和对象ID查询收藏
    Optional<Favorite> findByUserIdAndObjectId(Long userId, Long objectId);
    List<Favorite> findByUserIdAndObjectIdAndFavoriteType(Long userId, Long objectId, String favoriteType);

    boolean existsByUserIdAndObjectIdAndFavoriteType(Long userId, Long objectId, String favoriteType);

    // 根据用户ID、收藏类型和对象ID删除收藏
    void deleteByUserIdAndObjectIdAndFavoriteType(Long userId, Long objectId, String favoriteType);

    List<Favorite> findAllByUserIdAndGroupId(Long userId, Long groupId);


    @Modifying
    @Query("UPDATE Favorite f SET f.groupId = :newGroupId WHERE f.userId = :userId AND f.groupId = :oldGroupId")
    int updateGroupById(@Param("userId") Long userId, @Param("oldGroupId") Long oldGroupId, @Param("newGroupId") Long newGroupId);

    List<Favorite> findByUserIdAndGroupId(Long userId, Long groupId);

    List<Favorite> findAllByUserId(Long userId);

    // 根据 objectId 查询所有收藏记录
    Set<UserIdProjection> findByObjectId(Long objectId);
}