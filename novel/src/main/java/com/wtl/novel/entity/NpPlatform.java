package com.wtl.novel.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class NpPlatform {

    private String platformName;
    private String url;
    private Filter filter;

    /* ====== getter / setter ====== */
    public String getPlatformName() {
        return platformName;
    }
    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }


    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public Filter getFilter() {
        return filter;
    }
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "Platform{" +
                "platformName='" + platformName + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}