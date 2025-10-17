package com.wtl.novel.entity;

import com.wtl.novel.util.CompressionUtils;
import jakarta.persistence.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Entity
@Table(name = "user_chapter_edit",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_chapter",
                columnNames = {"user_id", "chapter_id"}))
public class UserChapterEdit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", nullable = false, length = 64)
    private String username;

    // longblob 对应 byte[]
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "content")
    private byte[] content;


    /* ===== 以下为 Getter/Setter ===== */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChapterId() { return chapterId; }
    public void setChapterId(Long chapterId) { this.chapterId = chapterId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @Transient
    public String getContent() {
        try {
            return content != null ?
                    CompressionUtils.decompress(content) : "";
        } catch (IOException e) {
            throw new RuntimeException("解压失败", e);
        }
    }

    // 设置内容（自动压缩）
    public void setContent(String text) {
        try {
            this.content = (text != null && !text.isEmpty()) ?
                    CompressionUtils.compress(text) : null;
        } catch (IOException e) {
            // 压缩失败时直接存储原始字节
            this.content = text.getBytes(StandardCharsets.UTF_8);
        }
    }
}