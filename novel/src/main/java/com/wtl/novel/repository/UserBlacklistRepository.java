package com.wtl.novel.repository;


import com.wtl.novel.entity.UserBlacklist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserBlacklistRepository extends JpaRepository<UserBlacklist, Long> {
    @Modifying
    @Transactional
    @Query("update UserBlacklist b set b.username = :newName where b.blockedId = :blockedId")
    int updateUsernameByBlockedId(@Param("blockedId") Long blockedId,
                                  @Param("newName") String newName);
    // 查询某用户的拉黑列表（分页）
    Page<UserBlacklist> findByUserId(Long userId, Pageable pageable);
    List<UserBlacklist> findByUserId(Long userId);

    // 删除一条拉黑记录
    void deleteByUserIdAndBlockedId(Long userId, Long blockedId);

    // 判断是否已经拉黑
    boolean existsByUserIdAndBlockedId(Long userId, Long blockedId);
    Optional<UserBlacklist> findByUserIdAndBlockedId(Long userId, Long blockedId);
}