package com.wtl.novel.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "platform_api_key")
public class PlatformApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = Platform.class)
    @JoinColumn(name = "platform_id", referencedColumnName = "id", nullable = false)
    private Platform platform;

    @Column(name = "api_key", nullable = false)
    private String apiKey;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // 构造方法
    public PlatformApiKey() {
    }

    public PlatformApiKey(Platform platform, String apiKey) {
        this.platform = platform;
        this.apiKey = apiKey;
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}