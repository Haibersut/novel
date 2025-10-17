package com.wtl.novel.CDO;

public class NoteCTO {
    private Long novelId;
    private String title;

    public NoteCTO(Long novelId, String title) {
        this.novelId = novelId;
        this.title = title;
    }

    public Long getNovelId() {
        return novelId;
    }

    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
