package com.wtl.novel.CDO;

import com.wtl.novel.entity.Novel;

import java.util.List;

public class NovelAndTagCTO extends Novel {
    private List<String> tags;

    public NovelAndTagCTO(Novel novel, List<String> tags) {
        // 使用 set 方法将 novel 的字段值赋给当前对象
        this.setId(novel.getId());
        this.setTitle(novel.getTitle());
        this.setUp(novel.getUp());
        this.setFontNumber(novel.getFontNumber());
        this.setPlatform(novel.getPlatform());
        // 设置 tags
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}