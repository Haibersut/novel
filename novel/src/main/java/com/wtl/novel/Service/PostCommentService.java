package com.wtl.novel.Service;

import com.wtl.novel.CDO.CommentTree;
import com.wtl.novel.entity.*;
import com.wtl.novel.repository.*;
import com.wtl.novel.util.MaskStringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostCommentService {

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserBlacklistRepository userBlacklistRepository;

    // 获取评论树（顶级评论及其子评论）
    public List<CommentTree> getCommentTreeByPostId(Long userId,Long postId) {
        // 获取所有评论
        List<PostComment> allComments = postCommentRepository.findByPostId(postId);
        List<UserBlacklist> blacklists = userBlacklistRepository.findByUserId(userId);
        Set<Long> list = blacklists.stream().map(UserBlacklist::getBlockedId).collect(Collectors.toSet());
        allComments.forEach(c -> {
            if (list.contains(c.getUserId())) {
                c.setContent("内容已被折叠");
            }
        });
        // 对顶级评论的用户名进行脱敏
//        allComments.forEach(commentTree -> {
//            commentTree.setUsername(MaskStringUtil.maskMiddle(commentTree.getUsername()));
//        });

        // 将评论转换为 CommentTree 类型
        List<CommentTree> commentTrees = allComments.stream()
                .map(CommentTree::new)
                .toList();

        // 构建评论树
        Map<Long, CommentTree> commentMap = new HashMap<>();
        for (CommentTree comment : commentTrees) {
            commentMap.put(comment.getId(), comment);
        }

        // 为每个评论添加子评论
        List<CommentTree> rootComments = new ArrayList<>();
        for (CommentTree comment : commentTrees) {
            if (comment.getParentId() == null) {
                // 如果是顶级评论，直接加入根列表
                rootComments.add(comment);
            } else {
                // 如果是子评论，找到父评论并添加到父评论的 children 中
                CommentTree parentComment = commentMap.get(comment.getParentId());
                if (parentComment != null) {
                    parentComment.addChild(comment);
                }
            }
        }

        return rootComments;
    }


    public List<PostComment> getAllCommentsByPostId(Long postId) {
        List<PostComment> byPostId = postCommentRepository.findByPostId(postId);
        byPostId.forEach(postComment -> {
            postComment.setUsername(MaskStringUtil.maskMiddle(postComment.getUsername()));
        });
        return byPostId;
    }

    public PostComment createComment(PostComment comment) {
        PostComment save = postCommentRepository.save(comment);
        Post post = postRepository.getReferenceById(save.getPostId());
        User byEmail = save.getReplyTo() != null ? userRepository.findByEmail(save.getReplyTo()) : null;
        if (save.getReplyTo() != null && byEmail != null && byEmail.getId() - save.getUserId() != 0) {
            Message message = new Message();
            message.setTextNum("");
            message.setPostId(comment.getPostId());
            message.setCreateTime(LocalDateTime.now());
            message.setMessageType(Message.MessageType.COMMENT);
            message.setPostCommentId(save.getId());
            message.setMessageContent(save.getContent());
            message.setExecuteUserId(save.getUserId());
            message.setUserId(byEmail.getId());
            message.setRead(false);
            User userById = userRepository.findUserById(save.getUserId());
            if (userById != null) {
                message.setUsername(userById.getEmail());
            }
            messageRepository.save(message);
        } else if (post.getUserId() - save.getUserId() != 0){
            Message message = new Message();
            message.setTextNum("");
            message.setPostId(comment.getPostId());
            message.setCreateTime(LocalDateTime.now());
            message.setMessageType(Message.MessageType.COMMENT);
            message.setPostCommentId(save.getId());
            message.setMessageContent(save.getContent());
            message.setExecuteUserId(save.getUserId());
            message.setUserId(post.getUserId());
            message.setRead(false);
            User userById = userRepository.findUserById(save.getUserId());
            message.setUsername(userById.getEmail());
            messageRepository.save(message);
        }
        postRepository.incrementCommentNumById(comment.getPostId());
        return save;
    }

    public PostComment likeComment(Long id) {
        PostComment comment = postCommentRepository.findById(id).orElse(null);
        if (comment != null) {
            comment.setLikes(comment.getLikes() + 1);
            return postCommentRepository.save(comment);
        }
        return null;
    }

    public List<PostComment> getRepliesByCommentId(Long parentId) {
        return postCommentRepository.findByParentId(parentId);
    }
}