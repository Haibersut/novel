package com.wtl.novel.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "post_agree",
        uniqueConstraints = @UniqueConstraint(name = "uk_post_user", columnNames = {"post_id", "user_id"})
)
public class PostAgree {
    public PostAgree() {
    }

    public PostAgree(Long id, Long postId, Long userId, Boolean agree) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.agree = agree;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "agree", nullable = false)
    private Boolean agree;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getAgree() {
        return agree;
    }

    public void setAgree(Boolean agree) {
        this.agree = agree;
    }
}