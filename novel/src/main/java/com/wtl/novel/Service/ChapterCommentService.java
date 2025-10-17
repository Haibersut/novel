package com.wtl.novel.Service;

import com.wtl.novel.CDO.ChapterCommentTree;
import com.wtl.novel.CDO.CommentTree;
import com.wtl.novel.DTO.TextNumCount;
import com.wtl.novel.entity.*;
import com.wtl.novel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ChapterCommentService {
    @Autowired
    private ChapterCommentRepository chapterCommentRepository;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserBlacklistRepository userBlacklistRepository;

    @Transactional
    public void moveComments(Long chapterId, List<Integer> oldTextNums, Integer newTextNum) {
        // 先锁再改，避免并发脏读
        int rows = chapterCommentRepository.updateTextNumByChapterIdAndTextNumIn(chapterId, oldTextNums, newTextNum);
    }

    public List<TextNumCount> countByChapterIdGroupByTextNum(Long chapterId) {
        return chapterCommentRepository.countByChapterIdGroupByTextNum(chapterId);
    }


    public ChapterComment createComment(ChapterComment comment) {
        Chapter chapter = chapterRepository.findById(comment.getChapterId()).get();
        comment.setNovelId(chapter.getNovelId());
        comment.setCreatedAt(LocalDateTime.now());
        ChapterComment save = chapterCommentRepository.save(comment);
        User byEmail = userRepository.findByEmail(save.getReplyTo());
        if (save.getReplyTo() != null && byEmail.getId() - save.getUserId() != 0) {
            Message message = new Message();
            message.setTextNum(String.valueOf(comment.getTextNum()));
            message.setPostId(comment.getChapterId());
            message.setCreateTime(LocalDateTime.now());
            message.setMessageType(Message.MessageType.COMMENT);
            message.setPostCommentId(save.getId());
            message.setMessageContent(save.getContent());
            message.setExecuteUserId(save.getUserId());
            message.setUserId(byEmail.getId());
            message.setRead(false);
            User userById = userRepository.findUserById(save.getUserId());
            message.setUsername(userById.getEmail());
            messageRepository.save(message);
        }
        return save;
    }


    public List<ChapterCommentTree> getCommentTreeByPostId(Long userId, Long chapterId, Integer textNum) {
        // 获取所有评论
        List<ChapterComment> allComments = chapterCommentRepository.findAllByChapterIdAndTextNum(chapterId,textNum);
        List<UserBlacklist> blacklists = userBlacklistRepository.findByUserId(userId);
        Set<Long> list = blacklists.stream().map(UserBlacklist::getBlockedId).collect(Collectors.toSet());
        allComments.forEach(c -> {
            if (list.contains(c.getUserId())) {
                c.setContent("内容已被折叠");
            }
        });

        // 将评论转换为 CommentTree 类型
        List<ChapterCommentTree> commentTrees = allComments.stream()
                .map(ChapterCommentTree::new)
                .toList();

        // 构建评论树
        Map<Long, ChapterCommentTree> commentMap = new HashMap<>();
        for (ChapterCommentTree comment : commentTrees) {
            commentMap.put(comment.getId(), comment);
        }

        // 为每个评论添加子评论
        List<ChapterCommentTree> rootComments = new ArrayList<>();
        for (ChapterCommentTree comment : commentTrees) {
            if (comment.getParentId() == null) {
                // 如果是顶级评论，直接加入根列表
                rootComments.add(comment);
            } else {
                // 如果是子评论，找到父评论并添加到父评论的 children 中
                ChapterCommentTree parentComment = commentMap.get(comment.getParentId());
                if (parentComment != null) {
                    parentComment.addChild(comment);
                }
            }
        }

        return rootComments;
    }


    public List<ChapterComment> getRepliesByCommentId(Long parentId) {
        return chapterCommentRepository.findByParentId(parentId);
    }
}
