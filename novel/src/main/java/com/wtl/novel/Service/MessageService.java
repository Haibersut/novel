package com.wtl.novel.Service;

import com.wtl.novel.DTO.NovelSimpleDto;
import com.wtl.novel.entity.Message;
import com.wtl.novel.entity.Novel;
import com.wtl.novel.repository.MessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository repo;

    public void newChapterMessage1(Set<Long> userIds, NovelSimpleDto novel) {
        List<Message> messageList = new ArrayList<>();
        for (Long userId : userIds) {
            Message message = new Message();
            message.setTextNum("");
            message.setPostId(novel.id());
            message.setCreateTime(LocalDateTime.now());
            message.setMessageType(Message.MessageType.SYSTEM);
            message.setPostCommentId(userId);
            message.setMessageContent("你收藏的【" + novel.title() + "】更新了新章节，现在可以获取新章节了");
            message.setExecuteUserId(userId);
            message.setUserId(userId);
            message.setRead(false);
            message.setUsername("系统消息");
            messageList.add(message);
        }
        repo.saveAll(messageList);
    }

    public void newChapterMessage(Set<Long> userIds, Novel novel) {
        List<Message> messageList = new ArrayList<>();
        for (Long userId : userIds) {
            Message message = new Message();
            message.setTextNum("");
            message.setPostId(novel.getId());
            message.setCreateTime(LocalDateTime.now());
            message.setMessageType(Message.MessageType.SYSTEM);
            message.setPostCommentId(userId);
            message.setMessageContent("你收藏的【" + novel.getTitle() + "】更新了新章节，最快将在一分钟内执行汉化");
            message.setExecuteUserId(userId);
            message.setUserId(userId);
            message.setRead(false);
            message.setUsername("系统消息");
            messageList.add(message);
        }
        repo.saveAll(messageList);
    }

    public MessageService(MessageRepository repo) {
        this.repo = repo;
    }


    /**
     * 把某个用户的所有未读消息一键置为已读
     */
    @Transactional
    public void markAllRead(Long userId) {
        repo.updateReadStatusByUserId(userId);
    }

    /**
     * 把某一条消息置为已读（前提是这条消息属于当前用户）
     */
    @Transactional
    public void markOneRead(Long userId, Long msgId) {
        Message msg = repo.findById(msgId)
                .orElseThrow(() -> new IllegalArgumentException("消息不存在"));
        if (!msg.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权操作他人消息");
        }
        if (Boolean.TRUE.equals(msg.getRead())) {
            return;   // 已经是已读，直接返回，避免无意义更新
        }
        msg.setRead(true);
        repo.save(msg);   // 也可以用 @Modifying 写原生 SQL，这里简单 save 即可
    }


    /**
     * 分页获取某用户的消息
     *
     * @param messageType
     * @param userId      接收人
     * @param read        true=已读  false=未读  null=全部
     * @param page        第几页，从 0 开始
     * @param size        每页条数
     */
    public Page<Message> pageMessage( Long userId, Boolean read, int page, int size) {
        // 快速防大页卡死
        if (size > 200) size = 200;
        return repo.findByUserIdAndReadOrderByCreateTimeDesc(userId, read, PageRequest.of(page, size));
    }
    public Page<Message> pageMessageByType(String messageType, Long userId, Boolean read, int page, int size) {
        // 快速防大页卡死
        if (size > 200) size = 200;
        if (Objects.equals(messageType, "SYSTEM")) {
            return repo.findByUserIdAndReadAndMessageTypeOrderByCreateTimeDesc(userId, read, Message.MessageType.valueOf(messageType), PageRequest.of(page, size));
        }else {
            List<Message.MessageType> nonSystem = Arrays.stream(Message.MessageType.values())
                    .filter(t -> t != Message.MessageType.SYSTEM)
                    .toList();
            return repo.findByUserIdAndReadAndMessageTypeInOrderByCreateTimeDesc(userId, read,nonSystem, PageRequest.of(page, size));
        }
    }

}