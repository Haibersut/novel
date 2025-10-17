package com.wtl.novel.DTO;

import java.util.Date;

public interface ChapterProjection {
    Long getId();
    String getTitle();
    int getChapterNumber();
    Long getNovelId();
    Date getUpdatedAt();
    boolean getOwnPhoto();
}