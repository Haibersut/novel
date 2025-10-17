package com.wtl.novel.CDO;

import java.util.List;

public class NpNovel {
    List<SimpleNovel> simpleNovelList;
    List<NovelBox> novelBoxes;

    public NpNovel() {
    }

    public NpNovel(List<SimpleNovel> simpleNovelList, List<NovelBox> novelBoxes) {
        this.simpleNovelList = simpleNovelList;
        this.novelBoxes = novelBoxes;
    }

    public List<NovelBox> getNovelBoxes() {
        return novelBoxes;
    }

    public void setNovelBoxes(List<NovelBox> novelBoxes) {
        this.novelBoxes = novelBoxes;
    }

    public List<SimpleNovel> getSimpleNovelList() {
        return simpleNovelList;
    }

    public void setSimpleNovelList(List<SimpleNovel> simpleNovelList) {
        this.simpleNovelList = simpleNovelList;
    }
}
