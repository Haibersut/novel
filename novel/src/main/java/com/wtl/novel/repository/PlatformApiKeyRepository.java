package com.wtl.novel.repository;

import com.wtl.novel.entity.PlatformApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatformApiKeyRepository extends JpaRepository<PlatformApiKey, Long> {
    // 根据平台ID和是否删除查找API密钥
    List<PlatformApiKey> findByPlatformIdAndIsDeletedFalse(Long platformId);
}