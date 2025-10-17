package com.wtl.novel.repository;


import com.wtl.novel.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatformRepository extends JpaRepository<Platform, Long> {
    Platform findPlatformByPlatformName(String platformName);
    // 新增方法：根据 platform_type 获取 Platform
    List<Platform> findPlatformsByPlatformType(String platformType);
}