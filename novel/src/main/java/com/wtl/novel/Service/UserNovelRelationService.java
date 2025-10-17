package com.wtl.novel.Service;

import com.wtl.novel.entity.UserNovelRelation;
import com.wtl.novel.repository.UserNovelRelationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserNovelRelationService {

    @Autowired
    private UserNovelRelationRepository userNovelRelationRepository;

    public List<UserNovelRelation> getUserNovelRelations(Long userId, String platform) {
        return userNovelRelationRepository.findByUserIdAndPlatform(userId, platform);
    }
    public List<UserNovelRelation> getNovelDetail(Long novelId,String platform,Long userId) {
        return userNovelRelationRepository.findByUserIdAndPlatformAndNovelId(userId, platform, novelId);
    }


    // 保存用户和小说的关系
    public UserNovelRelation saveUserNovelRelation(UserNovelRelation relation) {
        return userNovelRelationRepository.save(relation);
    }

    // 根据用户ID查询所有记录
    public List<UserNovelRelation> findByUserId(Long userId) {
        return userNovelRelationRepository.findByUserId(userId);
    }

    // 根据小说ID查询所有记录
    public List<UserNovelRelation> findByNovelId(Long novelId) {
        return userNovelRelationRepository.findByNovelId(novelId);
    }

    // 根据用户ID和小说ID查询记录
    public Optional<UserNovelRelation> findByUserIdAndNovelId(Long userId, Long novelId) {
        return userNovelRelationRepository.findByUserIdAndNovelId(userId, novelId);
    }

    // 检查是否存在用户ID和小说ID的记录
    public boolean existsByUserIdAndNovelId(Long userId, Long novelId) {
        return userNovelRelationRepository.existsByUserIdAndNovelId(userId, novelId);
    }

    // 根据用户ID和小说ID删除记录
    public void deleteByUserIdAndNovelId(Long userId, Long novelId) {
        userNovelRelationRepository.deleteByUserIdAndNovelId(userId, novelId);
    }
}