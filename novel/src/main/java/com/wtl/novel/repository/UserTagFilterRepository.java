package com.wtl.novel.repository;

import com.wtl.novel.entity.UserTagFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserTagFilterRepository extends JpaRepository<UserTagFilter, Long> {
    List<UserTagFilter> findAllByUserId(Long userId);
    Optional<UserTagFilter> findByUserIdAndTagId(Long userId, Long tagId);

    Optional<UserTagFilter> findByIdAndUserId(Long id, Long userId);
}