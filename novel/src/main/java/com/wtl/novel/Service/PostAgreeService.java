package com.wtl.novel.Service;

import com.wtl.novel.entity.Message;
import com.wtl.novel.entity.Post;
import com.wtl.novel.entity.PostAgree;
import com.wtl.novel.entity.User;
import com.wtl.novel.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PostAgreeService {

    private final PostAgreeRepository postAgreeRepository;
    private final PostRepository postRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final DictionaryRepository dictionaryRepository;
    public PostAgreeService(PostAgreeRepository repository, PostAgreeRepository postAgreeRepository, PostRepository postRepository, MessageRepository messageRepository, UserRepository userRepository, DictionaryRepository dictionaryRepository) {
        this.postAgreeRepository = postAgreeRepository;
        this.postRepository = postRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.dictionaryRepository = dictionaryRepository;
    }

    public Integer getPostAgreeByUserId(Long postId,Long userId) {
        Optional<PostAgree> opt = postAgreeRepository.findByPostIdAndUserId(postId, userId);
        if (opt.isPresent()) {
            PostAgree pa = opt.get();
            return pa.getAgree()?1:0;
        }
        return 2;
    }

    /**
     * 点赞/取消点赞（幂等）
     * 如果记录已存在则更新 agree，不存在则插入
     */
    @Transactional
    public void agree(Long postId, Long userId, String agree) {
        Post post = postRepository.getReferenceById(postId);
        if (!"false".equalsIgnoreCase(agree)
                && !"true".equalsIgnoreCase(agree)
                && !"no".equalsIgnoreCase(agree)) {
            return;
        }


        Long userId1 = post.getUserId();
        User userById = userRepository.findUserById(userId1);
        Optional<PostAgree> opt = postAgreeRepository.findByPostIdAndUserId(postId, userId);

        if ("no".equalsIgnoreCase(agree)) {                       // 取消投票
            opt.ifPresent(pa -> {
                if (Boolean.TRUE.equals(pa.getAgree())) {
                    postRepository.decrAgree(postId);
                    Integer agreePostPoint = Integer.valueOf(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("agreePostPoint").getValueField());
                    userById.setPoint(userById.getPoint() - agreePostPoint);
                    userRepository.save(userById);
                } else {
                    Integer agreePostPoint = Integer.valueOf(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("agreePostPoint").getValueField());
                    userById.setPoint(userById.getPoint() + agreePostPoint);
                    userRepository.save(userById);
                    postRepository.decrDisagree(postId);
                }
                postAgreeRepository.delete(pa);
            });
            return;
        }
        Boolean agree1 = Boolean.valueOf(agree);
        // 已有记录
        if (opt.isPresent()) {
            PostAgree pa = opt.get();
            Boolean old = pa.getAgree();
            if (old.equals(agree1)) {             // 没变化
                return;
            }
            // 先减旧值
            if (old) {
                postRepository.decrAgree(postId);
                Integer agreePostPoint = Integer.valueOf(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("agreePostPoint").getValueField());
                userById.setPoint(userById.getPoint() - agreePostPoint);
                userRepository.save(userById);
            } else {
                postRepository.decrDisagree(postId);
                Integer agreePostPoint = Integer.valueOf(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("agreePostPoint").getValueField());
                userById.setPoint(userById.getPoint() + agreePostPoint);
                userRepository.save(userById);
            }
            // 再加新值
            if (agree1) {
                postRepository.incrAgree(postId);
                Integer agreePostPoint = Integer.valueOf(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("agreePostPoint").getValueField());
                userById.setPoint(userById.getPoint() + agreePostPoint);
                userRepository.save(userById);
            } else {
                Integer agreePostPoint = Integer.valueOf(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("agreePostPoint").getValueField());
                userById.setPoint(userById.getPoint() - agreePostPoint);
                userRepository.save(userById);
                postRepository.incrDisagree(postId);
            }
            pa.setAgree(agree1);                  // 更新字段
            postAgreeRepository.save(pa);
        } else {                                // 首次投票
            postAgreeRepository.save(new PostAgree(null, postId, userId, agree1));
            if (agree1) {
                postRepository.incrAgree(postId);
                Integer agreePostPoint = Integer.valueOf(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("agreePostPoint").getValueField());
                userById.setPoint(userById.getPoint() + agreePostPoint);
                userRepository.save(userById);
                if (userId - post.getUserId() != 0) {
                    Message message = new Message();
                    message.setTextNum("");
                    message.setPostId(postId);
                    message.setCreateTime(LocalDateTime.now());
                    message.setMessageType(Message.MessageType.LIKE);
                    message.setPostCommentId(null);
                    message.setMessageContent("");
                    message.setExecuteUserId(userId);
                    message.setUserId(post.getUserId());
                    message.setRead(false);
                    User userById1 = userRepository.findUserById(userId);
                    message.setUsername(userById1.getEmail());
                    messageRepository.save(message);
                }
            } else {
                Integer agreePostPoint = Integer.valueOf(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("agreePostPoint").getValueField());
                userById.setPoint(userById.getPoint() - agreePostPoint);
                userRepository.save(userById);
                postRepository.incrDisagree(postId);
            }
        }
    }

}