package com.wtl.novel.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_blacklist",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_block",
                columnNames = {"user_id", "blocked_id"}))
public class UserBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;          // 主动拉黑的人

    @Column(name = "blocked_id", nullable = false)
    private Long blockedId;       // 被拉黑的人
    @Column(name = "username", nullable = false)
    private String username;       // 被拉黑的人

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /* === Getter / Setter / Constructor 省略，可用 Lombok === */
    public UserBlacklist() {}

    public UserBlacklist(Long userId, Long blockedId, String username) {
        this.userId = userId;
        this.blockedId = blockedId;
        this.username = username;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setBlockedId(Long blockedId) {
        this.blockedId = blockedId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getBlockedId() { return blockedId; }
    public LocalDateTime getCreateTime() { return createTime; }
}