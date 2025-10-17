package com.wtl.novel.CDO;


import com.wtl.novel.entity.Novel;

import java.time.LocalDateTime;
import java.util.List;

public class NovelCTO {
    private Long id;
    private String title;
    private String spans;
    private String trueName;
    private String photoUrl;
    private String novelType;
    private Long up;
    private String trueId;
    private Long fontNumber;
    private String platform;
    private Long novelRead;
    private Long novelLike;
    private boolean isDeleted = false;
    private Long lastChapter;
    private Long lastChapterId;
    private Long recommend;
    private Integer chapterNum;
    private List<String> tags;
    private String favoriteGroup;
    private String authorId;
    private String authorName;
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public NovelCTO(Novel novel, Long lastChapter, Long lastChapterId) {
        this.id = novel.getId();
        this.title = novel.getTitle();
        this.trueName = novel.getTrueName();
        this.photoUrl = novel.getPhotoUrl();
        this.novelType = novel.getNovelType();
        this.spans = novel.getSpans();
        this.up = novel.getUp();
        this.trueId = novel.getTrueId();
        this.fontNumber = novel.getFontNumber();
        this.platform = novel.getPlatform();
        this.isDeleted = novel.isDeleted();
        this.lastChapter = lastChapter;
        this.lastChapterId = lastChapterId;
        this.novelLike = novel.getNovelLike();
        this.novelRead = novel.getNovelRead();
        this.recommend = novel.getRecommend();
        this.authorId = novel.getAuthorId();
        this.authorName = novel.getAuthorName();
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Long getRecommend() {
        return recommend;
    }

    public void setRecommend(Long recommend) {
        this.recommend = recommend;
    }

    public Integer getChapterNum() {
        return chapterNum;
    }

    public void setChapterNum(Integer chapterNum) {
        this.chapterNum = chapterNum;
    }

    public String getFavoriteGroup() {
        return favoriteGroup;
    }

    public void setFavoriteGroup(String favoriteGroup) {
        this.favoriteGroup = favoriteGroup;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Long getNovelRead() {
        return novelRead;
    }

    public void setNovelRead(Long novelRead) {
        this.novelRead = novelRead;
    }

    public Long getNovelLike() {
        return novelLike;
    }

    public void setNovelLike(Long novelLike) {
        this.novelLike = novelLike;
    }

    public String getSpans() {
        return spans;
    }

    public void setSpans(String spans) {
        this.spans = spans;
    }

    public Long getLastChapterId() {
        return lastChapterId;
    }

    public void setLastChapterId(Long lastChapterId) {
        this.lastChapterId = lastChapterId;
    }

    public Long getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(Long lastChapter) {
        this.lastChapter = lastChapter;
    }

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

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
