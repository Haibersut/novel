package com.wtl.novel.CDO;

import java.util.List;

public class ChapterRequest {
    private String title;
    private String content;
    private List<ImageInfo> imgInfo;

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<ImageInfo> getImgInfo() {
        return imgInfo;
    }

    public void setImgInfo(List<ImageInfo> imgInfo) {
        this.imgInfo = imgInfo;
    }
}

