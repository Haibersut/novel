package com.wtl.novel.CDO;

public class ChapterContent {
    private String bookUrl;
    private int chapterNum;
    private String content;

    // 构造函数
    public ChapterContent() {
    }

    public ChapterContent(String bookUrl, int chapterNum, String content) {
        this.bookUrl = bookUrl;
        this.chapterNum = chapterNum;
        this.content = content;
    }

    // Getter和Setter方法
    public String getBookUrl() {
        return bookUrl;
    }

    public void setBookUrl(String bookUrl) {
        this.bookUrl = bookUrl;
    }

    public int getChapterNum() {
        return chapterNum;
    }

    public void setChapterNum(int chapterNum) {
        this.chapterNum = chapterNum;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ChapterContent{" +
                "bookUrl='" + bookUrl + '\'' +
                ", chapterNum=" + chapterNum +
                ", content='" + content + '\'' +
                '}';
    }
}