package com.wtl.novel.CDO;

import java.util.List;

public class BookMetaDto {

    private String name;
    private String bookUrl;
    private List<Integer> noGet;

    /* ====== getter / setter ====== */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBookUrl() {
        return bookUrl;
    }

    public void setBookUrl(String bookUrl) {
        this.bookUrl = bookUrl;
    }

    public List<Integer> getNoGet() {
        return noGet;
    }

    public void setNoGet(List<Integer> noGet) {
        this.noGet = noGet;
    }
}