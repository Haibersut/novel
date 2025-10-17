package com.wtl.novel.Service;

import com.wtl.novel.entity.Tag;
import com.wtl.novel.entity.User;
import com.wtl.novel.entity.UserTagFilter;
import com.wtl.novel.repository.TagRepository;
import com.wtl.novel.repository.UserTagFilterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserTagFilterService {

    public void filterTag(Long id,Long userId) {
        Optional<UserTagFilter> tagFilter = repository.findByIdAndUserId(id,userId);
        if (tagFilter.isPresent()) {
            repository.delete(tagFilter.get());
        } else {
            Tag tag = tagRepository.getReferenceById(id);
            UserTagFilter userTagFilter = new UserTagFilter();
            userTagFilter.setUserId(userId);
            userTagFilter.setTagId(id);
            userTagFilter.setTagName(tag.getName());
            repository.save(userTagFilter);
        }
    }
    public List<UserTagFilter> getFilterTag(Long userId) {
        return repository.findAllByUserId(userId);
    }

    private final UserTagFilterRepository repository;
    private final TagRepository tagRepository;

    public UserTagFilterService(UserTagFilterRepository repository, TagRepository tagRepository) {
        this.repository = repository;
        this.tagRepository = tagRepository;
    }

}