package com.wtl.novel.CDO;

import com.wtl.novel.entity.Note;

import java.util.List;

public class NoteGroupByChapter {
    private int chapterNumber;
    private Long chapterId;
    private List<Note> notes;

    public NoteGroupByChapter(int chapterNumber, Long chapterId,List<Note> notes) {
        this.chapterNumber = chapterNumber;
        this.chapterId = chapterId;
        this.notes = notes;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    // Getters and Setters
    public int getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }
}