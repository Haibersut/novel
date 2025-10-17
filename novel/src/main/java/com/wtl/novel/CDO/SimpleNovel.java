package com.wtl.novel.CDO;

import com.wtl.novel.entity.Novel;

import java.util.List;

public class SimpleNovel {
    private String novelName;
    private List<String> novelGenre;
    private String countView;
    private Integer countBook;
    private String writerNick;
    private NovelCTO novel;
    private String trueId;


    public SimpleNovel() {
    }

    public String getTrueId() {
        return trueId;
    }

    public void setTrueId(String trueId) {
        this.trueId = trueId;
    }

    public NovelCTO getNovel() {
        return novel;
    }

    public void setNovel(NovelCTO novel) {
        this.novel = novel;
    }

    public String getNovelName() {
        return novelName;
    }

    public void setNovelName(String novelName) {
        this.novelName = novelName;
    }

    public List<String> getNovelGenre() {
        return novelGenre;
    }

    public void setNovelGenre(List<String> novelGenre) {
        this.novelGenre = novelGenre;
    }

    public String getCountView() {
        return countView;
    }

    public void setCountView(String countView) {
        this.countView = countView;
    }

    public Integer getCountBook() {
        return countBook;
    }

    public void setCountBook(Integer countBook) {
        this.countBook = countBook;
    }

    public String getWriterNick() {
        return writerNick;
    }

    public void setWriterNick(String writerNick) {
        this.writerNick = writerNick;
    }
}