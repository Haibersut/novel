package com.wtl.novel.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "chapter_error_execute")
public class ChapterErrorExecute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "novel_id")
    private Long novelId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "chapter_number", nullable = false)
    private int chapterNumber;

    @Column(name = "content", nullable = false, columnDefinition = "mediumtext")
    private String content;
    @Column(name = "translator_content", columnDefinition = "mediumtext")
    private String translatorContent;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "now_state", nullable = false)
    private Integer nowState;

    @Column(name = "true_id")
    private String trueId; // 仅作为字段存储小说ID

    @Column(name = "own_photo")
    private boolean ownPhoto;
    @PrePersist
    protected void onCreate() {
        if (nowState == null) {
            nowState = 0;
        }
    }

    // 构造方法
    public ChapterErrorExecute() {
    }

    public ChapterErrorExecute(Long novelId, String title, int chapterNumber, String content, Integer nowState, String trueId, boolean ownPhoto) {
        this.novelId = novelId;
        this.title = title;
        this.chapterNumber = chapterNumber;
        this.content = content;
        this.nowState = nowState;
        this.trueId = trueId;
        this.ownPhoto = ownPhoto;
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

    public String getTranslatorContent() {
        return translatorContent;
    }

    public void setTranslatorContent(String translatorContent) {
        this.translatorContent = translatorContent;
    }

    // Getter 和 Setter 方法
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }


    public Integer getNowState() {
        return nowState;
    }

    public void setNowState(Integer nowState) {
        this.nowState = nowState;
    }

}