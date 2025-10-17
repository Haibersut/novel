package com.wtl.novel.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
public class Novel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 小说名称
    private String title;
    private String trueName;
    // 小说图片路径
    private String photoUrl;
    private String authorId;
    private String authorName;
    private String novelType;
    // 小说推荐数量
    private Long up;
    private String trueId;
    private Long fontNumber;

    private String platform;
    private String spans;
    private Long novelRead;
    private Long novelLike;
    private Long recommend;
    // 创建时间 —— 插入时写入，以后不再修改
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();

    // 更新时间 —— 每次 UPDATE 时由数据库自动刷新
    @Column(name = "updated_at", insertable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

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

    public String getTrueName() {
        return trueName;
    }

    public void setTrueName(String trueName) {
        this.trueName = trueName;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getTrueId() {
        return trueId;
    }

    public void setTrueId(String trueId) {
        this.trueId = trueId;
    }

    @Column(name = "is_deleted", columnDefinition = "TINYINT(1)")
    private boolean isDeleted = false;

    public boolean isDeleted() {
        return isDeleted;
    }


    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getUp() {
        return up;
    }

    public void setUp(Long up) {
        this.up = up;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNovelType() {
        return novelType;
    }

    public void setNovelType(String novelType) {
        this.novelType = novelType;
    }

    public Long getFontNumber() {
        return fontNumber;
    }

    public void setFontNumber(Long fontNumber) {
        this.fontNumber = fontNumber;
    }

    @Override
    public String toString() {
        return "Novel{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", trueName='" + trueName + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", novelType='" + novelType + '\'' +
                ", up=" + up +
                ", trueId='" + trueId + '\'' +
                ", fontNumber=" + fontNumber +
                ", platform='" + platform + '\'' +
                ", spans='" + spans + '\'' +
                ", novelRead='" + novelRead + '\'' +
                ", novelLike='" + novelLike + '\'' +
                ", isDeleted=" + isDeleted +
                '}';
    }
}