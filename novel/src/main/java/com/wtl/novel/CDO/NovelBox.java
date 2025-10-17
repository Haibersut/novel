package com.wtl.novel.CDO;

import java.util.List;

public class NovelBox {
    private String favCount;   // thumb_s4
    private String chapCount;  // thumb_s2
    private String bookName;   // cut_line_one
    private String author;     // font > font
    private String trueId;     // font > font
    private List<String> tags; // font > span(s)
    private NovelCTO novel;

    public NovelBox() {
    }

    public NovelCTO getNovel() {
        return novel;
    }

    public void setNovel(NovelCTO novel) {
        this.novel = novel;
    }

    public String getTrueId() {
        return trueId;
    }

    public void setTrueId(String trueId) {
        this.trueId = trueId;
    }

    public String getFavCount() {
        return favCount;
    }

    public void setFavCount(String favCount) {
        this.favCount = favCount;
    }

    public String getChapCount() {
        return chapCount;
    }

    public void setChapCount(String chapCount) {
        this.chapCount = chapCount;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
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