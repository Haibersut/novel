package com.wtl.novel.CDO;

public class NovelActiveMap {

    private String platformName;
    private String tag;
    private String plus;
    private String limit;
    private String time;
    private String sort;
    private String update;

    /* ---------- 构造 ---------- */
    public NovelActiveMap() {
    }

    public NovelActiveMap(String platformName,
                          String tag,
                          String plus,
                          String limit,
                          String time,
                          String sort,
                          String update) {
        this.platformName = platformName;
        this.tag = tag;
        this.plus = plus;
        this.limit = limit;
        this.time = time;
        this.sort = sort;
        this.update = update;
    }

    /* ---------- getter / setter ---------- */
    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getPlus() {
        return plus;
    }

    public void setPlus(String plus) {
        this.plus = plus;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    /* ---------- 打印 ---------- */
    @Override
    public String toString() {
        return "NovelActiveMap{" +
                "platformName='" + platformName + '\'' +
                ", tag='" + tag + '\'' +
                ", plus='" + plus + '\'' +
                ", limit='" + limit + '\'' +
                ", time='" + time + '\'' +
                ", sort='" + sort + '\'' +
                ", update='" + update + '\'' +
                '}';
    }
}