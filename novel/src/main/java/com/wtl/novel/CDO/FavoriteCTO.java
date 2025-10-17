package com.wtl.novel.CDO;

import com.wtl.novel.entity.Favorite;
import java.time.LocalDateTime;

public class FavoriteCTO {
    private Long id;
    private Long userId;
    private String favoriteType;
    private Long objectId;
    private String objectName;
    private LocalDateTime createdAt;
    private Long lastChapter;

    public FavoriteCTO(Favorite favorite, Long lastChapter) {
        this.id = favorite.getId();
        this.userId = favorite.getUserId();
        this.favoriteType = favorite.getFavoriteType();
        this.objectId = favorite.getObjectId();
        this.objectName = favorite.getObjectName();
        this.createdAt = favorite.getCreatedAt();
        this.lastChapter = lastChapter;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFavoriteType() {
        return favoriteType;
    }

    public void setFavoriteType(String favoriteType) {
        this.favoriteType = favoriteType;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getLastChapter() {
        return lastChapter;
    }

    public void setLastChapter(Long lastChapter) {
        this.lastChapter = lastChapter;
    }
}
