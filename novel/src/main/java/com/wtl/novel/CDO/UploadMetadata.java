package com.wtl.novel.CDO;

import java.util.List;

public class UploadMetadata {
    private String type;
    private String originalTitle;
    private String workTitle;
    private List<String> tags;
    private boolean uploadFile;

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getWorkTitle() {
        return workTitle;
    }

    public void setWorkTitle(String workTitle) {
        this.workTitle = workTitle;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(boolean uploadFile) {
        this.uploadFile = uploadFile;
    }
}