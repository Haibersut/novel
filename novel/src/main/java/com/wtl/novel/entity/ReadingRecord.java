package com.wtl.novel.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "reading_record")
public class ReadingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "novel_id")
    private Long novelId;

    @Column(name = "last_chapter")
    private Long lastChapter;

    @Column(name = "last_chapter_id")
    private Long lastChapterId;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getLastChapterId() {
        return lastChapterId;
    }

    public void setLastChapterId(Long lastChapterId) {
        this.lastChapterId = lastChapterId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getNovelId() {
        return novelId;
    }

    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }

    public Long getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(Long lastChapter) {
        this.lastChapter = lastChapter;
    }
}