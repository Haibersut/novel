package com.wtl.novel.entity;

public class FileInfo {
    private String epNumber;
    private int extractedNumber;
    private String chapterTitle;

    public FileInfo(String epNumber, int extractedNumber, String chapterTitle) {
        this.epNumber = epNumber;
        this.extractedNumber = extractedNumber;
        this.chapterTitle = chapterTitle;
    }

    public String getEpNumber() {
        return epNumber;
    }

    public int getExtractedNumber() {
        return extractedNumber;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }
}