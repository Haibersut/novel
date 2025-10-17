package com.wtl.novel.CDO;

import java.util.List;

public class NoteGroupByChapterWithNovelTitle {
    private String novelTitle;
    private int totalNotes;
    private List<NoteGroupByChapter> noteGroups;

    public NoteGroupByChapterWithNovelTitle(String novelTitle,int totalNotes, List<NoteGroupByChapter> noteGroups) {
        this.novelTitle = novelTitle;
        this.noteGroups = noteGroups;
        this.totalNotes = totalNotes;
    }

    public int getTotalNotes() {
        return totalNotes;
    }

    public void setTotalNotes(int totalNotes) {
        this.totalNotes = totalNotes;
    }

    // Getters and Setters
    public String getNovelTitle() {
        return novelTitle;
    }

    public void setNovelTitle(String novelTitle) {
        this.novelTitle = novelTitle;
    }

    public List<NoteGroupByChapter> getNoteGroups() {
        return noteGroups;
    }

    public void setNoteGroups(List<NoteGroupByChapter> noteGroups) {
        this.noteGroups = noteGroups;
    }
}