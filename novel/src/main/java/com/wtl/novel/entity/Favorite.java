package com.wtl.novel.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "favorite_type", nullable = false)
    private String favoriteType;

    @Column(name = "object_id", nullable = false)
    private Long objectId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "object_name", nullable = false)
    private String objectName;

    @CreationTimestamp // 自动生成创建时间
    @Column(updatable = false) // 确保该字段不可更新
    private LocalDateTime createdAt;

    public Favorite(Long userId, String favoriteType, Long objectId, String objectName) {
        this.userId = userId;
        this.favoriteType = favoriteType;
        this.objectId = objectId;
        this.objectName = objectName;
    }

    public Favorite() {

    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    // Getters and Setters
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}