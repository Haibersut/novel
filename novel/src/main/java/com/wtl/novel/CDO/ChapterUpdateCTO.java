package com.wtl.novel.CDO;

public class ChapterUpdateCTO {
    private Long chapterId; // 章节ID
    private String content; // 章节内容

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}