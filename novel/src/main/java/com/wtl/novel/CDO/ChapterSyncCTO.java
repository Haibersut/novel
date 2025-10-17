package com.wtl.novel.CDO;

import com.wtl.novel.entity.Chapter;
import com.wtl.novel.scalingUp.entity.ChapterScalingUpOne;

import java.util.List;

public class ChapterSyncCTO {
    private List<Chapter> chapters;
    private List<ChapterScalingUpOne> chapterScalingUpOnes;

    public ChapterSyncCTO() {
    }

    public ChapterSyncCTO(List<Chapter> chapters, List<ChapterScalingUpOne> chapterScalingUpOnes) {
        this.chapters = chapters;
        this.chapterScalingUpOnes = chapterScalingUpOnes;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public List<ChapterScalingUpOne> getChapterScalingUpOnes() {
        return chapterScalingUpOnes;
    }

    public void setChapterScalingUpOnes(List<ChapterScalingUpOne> chapterScalingUpOnes) {
        this.chapterScalingUpOnes = chapterScalingUpOnes;
    }
}
