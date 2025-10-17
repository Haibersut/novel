package com.wtl.novel.CDO;

import com.wtl.novel.entity.Novel;
import com.wtl.novel.entity.Tag;

import java.util.ArrayList;
import java.util.List;

public class UploadNovelDTO {
    private Long id;

    // 小说名称
    private String title;
    private String trueName;
    // 小说图片路径
    private String photoUrl;
    private String novelType;
    // 小说推荐数量
    private Long up;
    private String trueId;
    private Long fontNumber;

    private String platform;

    public UploadNovelDTO(Novel novel, List<Tag> tags) {
        this.id = novel.getId();
        this.title = novel.getTitle();
        this.trueName = novel.getTrueName();
        this.photoUrl = novel.getPhotoUrl();
        this.novelType = novel.getNovelType();
        this.up = novel.getUp();
        this.trueId = novel.getTrueId();
        this.fontNumber = novel.getFontNumber();
        this.platform = novel.getPlatform();
        this.tags = tags;
    }

    List<Tag> tags = new ArrayList<>();

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
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
}
