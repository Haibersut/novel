package com.wtl.novel.CDO;

import java.util.Date;

public class ReadingRecordCTO {
    private String title;
    private Long lastChapter;
    private Long id;
    private Date readDate;

    public ReadingRecordCTO(String title, Long lastChapter, Long id, Date readDate) {
        this.title = title;
        this.lastChapter = lastChapter;
        this.id = id;
        this.readDate = readDate;
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

    public Long getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(Long lastChapter) {
        this.lastChapter = lastChapter;
    }

    public Date getReadDate() {
        return readDate;
    }

    public void setReadDate(Date readDate) {
        this.readDate = readDate;
    }
}
