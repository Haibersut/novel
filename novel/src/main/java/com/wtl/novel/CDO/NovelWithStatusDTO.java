package com.wtl.novel.CDO;

import com.wtl.novel.entity.Novel;

import java.util.List;

public class NovelWithStatusDTO {
    private Long id;
    private String title;
    private String trueName;
    private String photoUrl;
    private String novelType;
    private Long up;
    private String trueId;
    private Long fontNumber;
    private String platform;
    private String spans;
    private boolean isDeleted;
    private List<String> tags;

    // 新增字段
    private Long lastChapter;   // 最后阅读章节
    private boolean isFavorite; // 是否收藏

    public NovelWithStatusDTO() {
    }

    // 从Novel实体复制基本属性的构造方法
    public NovelWithStatusDTO(Novel novel) {
        this.id = novel.getId();
        this.title = novel.getTitle();
        this.trueName = novel.getTrueName();
        this.photoUrl = novel.getPhotoUrl();
        this.novelType = novel.getNovelType();
        this.up = novel.getUp();
        this.trueId = novel.getTrueId();
        this.fontNumber = novel.getFontNumber();
        this.platform = novel.getPlatform();
        this.spans = novel.getSpans();
        this.isDeleted = novel.isDeleted();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTrueName() {
        return trueName;
    }

    public void setTrueName(String trueName) {
        this.trueName = trueName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getNovelType() {
        return novelType;
    }

    public void setNovelType(String novelType) {
        this.novelType = novelType;
    }

    public Long getUp() {
        return up;
    }

    public void setUp(Long up) {
        this.up = up;
    }

    public String getTrueId() {
        return trueId;
    }

    public void setTrueId(String trueId) {
        this.trueId = trueId;
    }

    public Long getFontNumber() {
        return fontNumber;
    }

    public void setFontNumber(Long fontNumber) {
        this.fontNumber = fontNumber;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getSpans() {
        return spans;
    }

    public void setSpans(String spans) {
        this.spans = spans;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Long getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(Long lastChapter) {
        this.lastChapter = lastChapter;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}