package com.wtl.novel.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_tag_filter",
        indexes = @Index(name = "idx_user_id", columnList = "user_id"))
public class UserTagFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tag_name", nullable = false)
    private String tagName;

    public UserTagFilter() {}

    public UserTagFilter(Long tagId, Long userId, String tagName) {
        this.tagId = tagId;
        this.userId = userId;
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}