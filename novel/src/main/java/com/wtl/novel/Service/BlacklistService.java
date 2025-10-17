package com.wtl.novel.Service;

import com.wtl.novel.entity.User;
import com.wtl.novel.entity.UserBlacklist;
import com.wtl.novel.repository.UserBlacklistRepository;
import com.wtl.novel.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BlacklistService {

    private final UserRepository userRepository;

    private final UserBlacklistRepository repo;

    public BlacklistService(UserRepository userRepository, UserBlacklistRepository repo) {
        this.userRepository = userRepository;
        this.repo = repo;
    }

    /**
     * 拉黑：若已存在则忽略
     */
    @Transactional
    public UserBlacklist block(Long userId, Long blockedId) {
        if (repo.existsByUserIdAndBlockedId(userId, blockedId)) {
            return repo.findByUserIdAndBlockedId(userId, blockedId).orElse(null);
        }
        User referenceById = userRepository.getReferenceById(blockedId);
        return repo.save(new UserBlacklist(userId, blockedId, referenceById.getEmail()));
    }

    /**
     * 取消拉黑
     */
    @Transactional
    public void unblock(Long userId, Long blockedId) {
        repo.deleteByUserIdAndBlockedId(userId, blockedId);
    }

    /**
     * 分页查询拉黑列表
     */
    public Page<UserBlacklist> list(Long userId, Pageable pageable) {
        return repo.findByUserId(userId, pageable);
    }
}