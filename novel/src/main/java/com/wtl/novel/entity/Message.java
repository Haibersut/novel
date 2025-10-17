package com.wtl.novel.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 创建时间 */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /** 接收消息的用户 ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "text_num", nullable = false)
    private String textNum;

    /** 触发消息的用户 ID */
    @Column(name = "execute_user_id", nullable = false)
    private Long executeUserId;

    /** 消息类型枚举 */
    public enum MessageType {
        LIKE(1),
        COMMENT(2),
        FOLLOW(3),
        SYSTEM(4);

        private final int code;

        MessageType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static MessageType of(int code) {
            for (MessageType t : values()) {
                if (t.code == code) return t;
            }
            throw new IllegalArgumentException("Unknown message type: " + code);
        }
    }

    /** 消息类型 */
    @Enumerated(EnumType.ORDINAL)   // 存 0、1、2、3
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    /** 消息内容（可选） */
    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;

    /** 关联帖子 ID（可选） */
    @Column(name = "post_id")
    private Long postId;

    /** 关联评论 ID（可选） */
    @Column(name = "post_comment_id")
    private Long postCommentId;

    /** 是否已读 */
    @Column(name = "is_read", nullable = false, columnDefinition = "TINYINT(1) default 0")
    private Boolean read = false;

    public String getTextNum() {
        return textNum;
    }

    public void setTextNum(String textNum) {
        this.textNum = textNum;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getExecuteUserId() {
        return executeUserId;
    }

    public void setExecuteUserId(Long executeUserId) {
        this.executeUserId = executeUserId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getPostCommentId() {
        return postCommentId;
    }

    public void setPostCommentId(Long postCommentId) {
        this.postCommentId = postCommentId;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }
}