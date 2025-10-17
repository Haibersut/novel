package com.wtl.novel.Service;

import com.wtl.novel.entity.NovelTag;
import com.wtl.novel.entity.Tag;
import com.wtl.novel.repository.NovelTagRepository;
import com.wtl.novel.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NovelTagService {

    @Autowired
    private NovelTagRepository novelTagRepository;
    @Autowired
    private TagRepository tagRepository;

    // 根据 novelId 分页查询 tagId 列表
    public List<String> findTagIdsByNovelId(Long novelId) {
        List<NovelTag> novelTags = novelTagRepository.findByNovelId(novelId);
        List<Long> tagIds = novelTags.stream().map(NovelTag::getTagId).toList();
        return tagRepository.findByIdIn(tagIds).stream().map(Tag::getName).toList();
    }

    public List<NovelTag> save(List<NovelTag> novelTagList) {
        List<NovelTag> toSave = new ArrayList<>();
        for (NovelTag novelTag : novelTagList) {
            if (novelTagRepository.findByNovelIdAndTagId(novelTag.getNovelId(), novelTag.getTagId()).isEmpty()) {
                toSave.add(novelTag);
            }
        }
        return novelTagRepository.saveAll(toSave);
    }
}