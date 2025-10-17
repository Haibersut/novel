package com.wtl.novel.CDO;

import java.util.List;

public class SyosetuNovelDetail {
    private String prologue;
    private String title;
    private String trueId;
    private String novelType;
    private List<String> tagList;

    public SyosetuNovelDetail(String prologue, String title, String trueId, List<String> tagList, String novelType) {
        this.prologue = prologue;
        this.title = title;
        this.trueId = trueId;
        this.tagList = tagList;
        this.novelType = novelType;
    }

    public String getNovelType() {
        return novelType;
    }

    public void setNovelType(String novelType) {
        this.novelType = novelType;
    }

    public String getPrologue() {
        return prologue;
    }

    public void setPrologue(String prologue) {
        this.prologue = prologue;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTrueId() {
        return trueId;
    }

    public void setTrueId(String trueId) {
        this.trueId = trueId;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
    }
}