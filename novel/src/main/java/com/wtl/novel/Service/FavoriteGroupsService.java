package com.wtl.novel.Service;

import com.wtl.novel.entity.FavoriteGroups;
import com.wtl.novel.repository.FavoriteGroupsRepository;
import com.wtl.novel.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FavoriteGroupsService {

    @Autowired
    private FavoriteGroupsRepository favoriteGroupsRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    // 创建分组
    public FavoriteGroups createFavoriteGroup(Long userId, String name) {
        FavoriteGroups group = new FavoriteGroups();
        group.setUserId(userId);
        group.setName(name);
        group.setCreatedAt(LocalDateTime.now());
        return favoriteGroupsRepository.save(group);
    }

    public FavoriteGroups updateFavoriteGroup(Long groupId, Long userId, String name) {
        FavoriteGroups group = favoriteGroupsRepository.getReferenceById(groupId);
        if (!group.getUserId().equals(userId)) {
            return null;
        }
        group.setName(name);
        return favoriteGroupsRepository.save(group);
    }

    // 获取用户的所有分组
    public List<FavoriteGroups> getAllFavoriteGroups(Long userId) {
        List<FavoriteGroups> favoriteGroups = new ArrayList<>();
        Optional<FavoriteGroups> byId = favoriteGroupsRepository.findById(0L);
        byId.ifPresent(favoriteGroups::add);
        favoriteGroups.addAll(favoriteGroupsRepository.findAllByUserId(userId));
        return favoriteGroups;
    }

    // 根据分组名称获取分组
    public FavoriteGroups getFavoriteGroupByName(Long userId, String name) {
        return favoriteGroupsRepository.findByNameAndUserId(name, userId)
                .orElseThrow(() -> new RuntimeException("分组不存在"));
    }

    // 删除分组
    @Transactional
    public void deleteFavoriteGroup(Long groupId, Long userId) {
        favoriteRepository.updateGroupById(userId, groupId, 0L);
        favoriteGroupsRepository.deleteById(groupId);
    }
}