package com.wtl.novel.CDO;

import java.util.List;

public class BookInfo {
    private String bookUrl;
    private List<ChapterInfo> chapters;

    // 构造函数
    public BookInfo() {
    }

    public BookInfo(String bookUrl, List<ChapterInfo> chapters) {
        this.bookUrl = bookUrl;
        this.chapters = chapters;
    }

    // 内部静态类
    public static class ChapterInfo {
        private int chapterNum;
        private String chapterUrl;

        public ChapterInfo() {
        }

        public ChapterInfo(int chapterNum, String chapterUrl) {
            this.chapterNum = chapterNum;
            this.chapterUrl = chapterUrl;
        }

        // Getter和Setter方法
        public int getChapterNum() {
            return chapterNum;
        }

        public void setChapterNum(int chapterNum) {
            this.chapterNum = chapterNum;
        }

        public String getChapterUrl() {
            return chapterUrl;
        }

        public void setChapterUrl(String chapterUrl) {
            this.chapterUrl = chapterUrl;
        }

        @Override
        public String toString() {
            return "ChapterInfo{" +
                    "chapterNum=" + chapterNum +
                    ", chapterUrl='" + chapterUrl + '\'' +
                    '}';
        }
    }

    // Getter和Setter方法
    public String getBookUrl() {
        return bookUrl;
    }

    public void setBookUrl(String bookUrl) {
        this.bookUrl = bookUrl;
    }

    public List<ChapterInfo> getChapters() {
        return chapters;
    }

    public void setChapters(List<ChapterInfo> chapters) {
        this.chapters = chapters;
    }

    @Override
    public String toString() {
        return "BookInfo{" +
                "bookUrl='" + bookUrl + '\'' +
                ", chapters=" + chapters +
                '}';
    }
}