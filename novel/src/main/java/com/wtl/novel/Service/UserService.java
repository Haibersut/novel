package com.wtl.novel.Service;

import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.Post;
import com.wtl.novel.entity.User;
import com.wtl.novel.repository.*;
import com.wtl.novel.util.CustomPasswordEncoder;
import com.wtl.novel.util.LockManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostCommentRepository postCommentRepository;
    @Autowired
    private InvitationCodeRepository invitationCodeRepository;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private UserChapterEditRepository userChapterEditRepository;
    @Autowired
    private UserBlacklistRepository userBlacklistRepository;
    @Autowired
    private ChapterCommentRepository chapterCommentRepository;

    @Autowired
    private CustomPasswordEncoder passwordEncoder;

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private DictionaryRepository dictionaryRepository;
    @Autowired
    private CredentialService credentialService;

    public User getUserByToken(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        String[] authorizationInfo = authHeader.split(";");
        if (authorizationInfo.length == 0) {
            return null;
        }
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        return credential != null ? credential.getUser() : null;
    }

    public Credential getCredentialByToken(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }
        String[] authorizationInfo = authHeader.split(";");
        if (authorizationInfo.length == 0) {
            return null;
        }
        String authorizationHeader = authorizationInfo[0];
        return credentialService.findByToken(authorizationHeader);
    }

    public boolean deductPoints(Long userId, Long points) {
        return userRepository.checkAndDecreasePoints(userId, points);
    }

    public int updateUploadByUserId(Long userId, Boolean upload) {
        return userRepository.updateUploadByUserId(userId, upload);
    };

    @Transactional
    public boolean rewardPoints(Long postId, Long fromUserId, Long points) {
        // 检查打赏积分是否有效
        if (points <= 0) {
            throw new IllegalArgumentException("打赏积分必须大于 0");
        }
        Post post = postRepository.findById(postId).get();
        Long toUserId = post.getUserId();
        if (fromUserId.equals(toUserId)) {
            return true;
        }
        // 查询打赏用户
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new RuntimeException("打赏用户不存在"));

        // 查询接收用户
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new RuntimeException("接收用户不存在"));

        // 检查打赏用户是否有足够的积分
        if (fromUser.getPoint() < points) {
            throw new RuntimeException("打赏用户积分不足");
        }

        // 扣除打赏用户的积分
        fromUser.setPoint(fromUser.getPoint() - points);

        // 增加接收用户的积分
        toUser.setPoint(toUser.getPoint() + points);

        // 保存两个用户
        userRepository.save(fromUser);
        userRepository.save(toUser);

        return true;
    }

    public User createUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPoint(0L);
        user.setPassword(passwordEncoder.encode(password));
        user.setHideReadBooks(false);
        user.setUpload(true);
        return userRepository.save(user);
    }

    public int modifyPassword(String password,User user) {
        if (CustomPasswordEncoder.isValid(password)) {
            return userRepository.updatePasswordByUserId(user.getId(), passwordEncoder.encode(password));
        }else {
            throw new RuntimeException("密码格式错误");
        }
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findUserById(Long userId) {
        return userRepository.findUserById(userId);
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public Long getUserPoint(Long userId) {
        return userRepository.findPointByUserId(userId);
    }

    @Transactional
    public String rename(String email, Long id) {

        String userLockKey = "rename_LOCK_USER_" + id;
        ReentrantLock lock = LockManager.getTryLock(userLockKey);

        try {
            /* 非阻塞：最多等 0 秒，抢不到立即返回 */
            if (!lock.tryLock()) {
                return "系统繁忙，请稍后再试";
            }
            email = email.trim();
            User userById = userRepository.findUserById(id);
            boolean b = userRepository.existsByEmail(email);
            if (b) {
                return "用户名已经被使用";
            }
            Integer reNamePoint = Integer.valueOf(dictionaryService.getDictionaryByKey("ReNamePoint").getValueField());
            if (userById.getPoint() < reNamePoint) {
                return "积分不足";
            }
            invitationCodeRepository.updateEmailByOldEmail(userById.getEmail(), email);

            postCommentRepository.updateUsernameByUserIdAndOldUsername(userById.getId(), email);
            chapterCommentRepository.updateUsernameByUserIdAndOldUsername(userById.getId(), email);


            userBlacklistRepository.updateUsernameByBlockedId(userById.getId(),email);
            userChapterEditRepository.updateUsernameByUserId(userById.getId(),email);
            postRepository.updateAuthorByUserId(userById.getId(),email);
            userById.setEmail(email);
            userById.setPoint(userById.getPoint() - reNamePoint);
            userRepository.save(userById);
            return "修改成功";
        } finally {
            lock.unlock();
        }
    }
}