package com.wtl.novel.scalingUp.entity;


import com.wtl.novel.entity.Chapter;
import com.wtl.novel.util.CompressionUtils;
import jakarta.persistence.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Entity
public class ChapterScalingUpOne {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long novelId; // 仅作为字段存储小说ID
    private String trueId; // 仅作为字段存储小说ID
    private boolean ownPhoto;

    @Column(name = "is_deleted", columnDefinition = "TINYINT(1)")
    private boolean isDeleted = false;

    private String title; // 章节标题
    private int chapterNumber; // 章节编号
    @Lob
    private byte[] content; // 存储压缩后的二进制数据

    public ChapterScalingUpOne() {
    }
    public ChapterScalingUpOne(Chapter chapter) {
        this.id = chapter.getId();
        this.novelId = chapter.getNovelId();
        this.trueId = chapter.getTrueId();
        this.ownPhoto = chapter.isOwnPhoto();
        this.isDeleted = chapter.isDeleted();
        this.title = chapter.getTitle();
        this.chapterNumber = chapter.getChapterNumber();
        // 直接拷贝压缩后的字节数组，避免再次压缩
        this.setContent(chapter.getContent());
        this.updatedAt = chapter.getUpdatedAt();
    }

    // 获取解压后的内容（不存储）
    @Transient
    public String getContent() {
        try {
            return content != null ?
                    CompressionUtils.decompress(content) : "";
        } catch (IOException e) {
            throw new RuntimeException("解压失败", e);
        }
    }

    // 设置内容（自动压缩）
    public void setContent(String text) {
        try {
            this.content = (text != null && !text.isEmpty()) ?
                    CompressionUtils.compress(text) : null;
        } catch (IOException e) {
            // 压缩失败时直接存储原始字节
            this.content = text.getBytes(StandardCharsets.UTF_8);
        }
    }
    // 添加 updated_at 字段
    @Column(name = "updated_at", nullable = false, updatable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrueId() {
        return trueId;
    }

    public void setTrueId(String trueId) {
        this.trueId = trueId;
    }

    public boolean isOwnPhoto() {
        return ownPhoto;
    }

    public void setOwnPhoto(boolean ownPhoto) {
        this.ownPhoto = ownPhoto;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Long getNovelId() {
        return novelId;
    }

    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }

    // 使用 @PrePersist 自动设置 createdAt、isDelete 和 isResolved 字段
    @PrePersist
    protected void onCreate() {
        updatedAt = new Date();
    }
}