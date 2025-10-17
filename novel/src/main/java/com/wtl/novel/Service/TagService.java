package com.wtl.novel.Service;

import com.wtl.novel.entity.Tag;
import com.wtl.novel.entity.UserTagFilter;
import com.wtl.novel.repository.NovelTagRepository;
import com.wtl.novel.repository.TagRepository;
import com.wtl.novel.repository.UserTagFilterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TagService {
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private UserTagFilterRepository userTagFilterRepository;

    @Autowired
    private NovelTagRepository novelTagRepository;
    
    public List<Tag> save(List<Tag> tags) {
        if (tags.isEmpty()) {
            return tags;
        }
        List<Tag> byPlatformAndNameIn = tagRepository.findByPlatformAndTrueNameIn(tags.get(0).getPlatform(), tags.stream().map(Tag::getTrueName).collect(Collectors.toList()));
        List<Tag> tagList = new ArrayList<>(byPlatformAndNameIn);

        // 从已有标签中移除已存在的标签
        List<Tag> tagsToSave = tags.stream()
                .filter(tag -> byPlatformAndNameIn.stream().noneMatch(existingTag -> existingTag.getTrueName().equals(tag.getTrueName())))
                .collect(Collectors.toList());

        // 保存不存在的标签
        List<Tag> savedTags = tagRepository.saveAll(tagsToSave);

        // 将已存在的标签和新保存的标签合并
        tagList.addAll(savedTags);

        return tagList;
    }

    public List<Tag> getAllTags() {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        return tagRepository.findAll(sort);
    }
    public List<Tag> getAllTagsByPlatform(String platform) {
        Pageable pageable = PageRequest.of(0, 1500);
        // tagRepository.findByPlatform(platform, Sort.by(Sort.Direction.ASC, "id"));
        List<Tag> byPlatformLimit = tagRepository.findByPlatformLimit(platform, pageable);
        List<Tag> byPlatformLimit1 = tagRepository.findByPlatformLimit("user", pageable);
        byPlatformLimit.addAll(byPlatformLimit1);

        return byPlatformLimit;
    }

    public List<Tag> allByKeyword(String keyword) {
        return tagRepository.allByKeyword(keyword);
    }

    public List<Tag> allByKeywordBugNoFilter(String keyword, Long userId) {
        List<Tag> tagList = tagRepository.allByKeyword(keyword);
        List<UserTagFilter> allByUserId = userTagFilterRepository.findAllByUserId(userId);

        // 提取需要过滤的 tagId 集合
        Set<Long> filteredTagIds = allByUserId.stream()
                .map(UserTagFilter::getTagId)
                .collect(Collectors.toSet());

        // 过滤掉存在于 filteredTagIds 中的 Tag
        return tagList.stream()
                .filter(tag -> !filteredTagIds.contains(tag.getId()))
                .collect(Collectors.toList());
    }

    public List<Tag> findByPlatformAnd(String platform, String keyword) {
        List<Tag> byPlatformAnd1 = tagRepository.findByPlatformAnd(platform, keyword);
        List<Tag> byPlatformAnd2 = tagRepository.findByPlatformAnd("user", keyword);
        List<Tag> tagList = new ArrayList<>();
        tagList.addAll(byPlatformAnd1);
        tagList.addAll(byPlatformAnd2);
        return tagList;
    }

    // 根据 novelId 获取所有 Tag 对象
    public List<String> getTagsByNovelId(Long novelId) {
        // 第一步：根据 novelId 查询所有 tagId
        List<Long> tagIds = novelTagRepository.findTagIdsByNovelId(novelId);

        // 第二步：根据 tagId 列表查询 Tag 对象
        return tagRepository.findByIdIn(tagIds).stream().map(Tag::getName).collect(Collectors.toList());
    }

    // 根据 novelId 获取所有 Tag 对象
    public List<Tag> getTagsAllInfoByNovelId(Long novelId) {
        // 第一步：根据 novelId 查询所有 tagId
        List<Long> tagIds = novelTagRepository.findTagIdsByNovelId(novelId);

        // 第二步：根据 tagId 列表查询 Tag 对象
        return tagRepository.findByIdIn(tagIds);
    }
}
