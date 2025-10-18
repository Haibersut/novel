package com.wtl.novel.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chapter_comment",
        indexes = @Index(name = "idx_chapter_text",   // 索引名
                columnList = "chapter_id, text_num"))  // 顺序敏感
public class ChapterComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long novelId;

    @Column(length = 50, nullable = false)
    private String username;

    @Column(length = 255)
    private String avatar;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    private Integer likes = 0;

    private Long parentId;

    @Column(length = 50)
    private String replyTo;

    @Column(name = "created_at", columnDefinition = "VARCHAR(50)")
    @Convert(converter = com.wtl.novel.util.TimestampConverter.class)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long chapterId;

    @Column(nullable = false)
    private Integer textNum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNovelId() {
        return novelId;
    }

    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public Integer getTextNum() {
        return textNum;
    }

    public void setTextNum(Integer textNum) {
        this.textNum = textNum;
    }
}