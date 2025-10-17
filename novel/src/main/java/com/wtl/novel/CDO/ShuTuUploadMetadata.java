package com.wtl.novel.CDO;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShuTuUploadMetadata {
    private String title;
    private String tagString;
    private String trueId;
    private String platform;
    private String author;
    private List<String> tags;

    public void genTags() {
        this.tags = Arrays.stream(tagString.split("\\s*,\\s*"))
                .filter(s -> !s.isEmpty())
                .map(String::trim)
                .toList();
    }

    public ShuTuUploadMetadata() {
    }

    public ShuTuUploadMetadata(String title, String tagString, String trueId, String platform, String author, List<String> tags) {
        this.title = title;
        this.tagString = tagString;
        this.trueId = trueId;
        this.platform = platform;
        this.author = author;
        this.tags = tags;
    }

    public String getTrueId() {
        return trueId;
    }

    public void setTrueId(String trueId) {
        this.trueId = trueId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTagString() {
        return tagString;
    }

    public void setTagString(String tagString) {
        this.tagString = tagString;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}