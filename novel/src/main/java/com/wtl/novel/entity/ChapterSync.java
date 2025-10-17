package com.wtl.novel.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.Comment;

/**
 * 章节同步状态表对应 JPA 实体
 */
@Entity
@Table(name = "chapter_sync")
public class ChapterSync {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键ID")
    private Long id;

    @Column(name = "chapter_id", nullable = false)
    @Comment("章节ID")
    private Long chapterId;

    @Column(name = "host_server_name", nullable = false, length = 128)
    @Comment("目标主机/服务器名称")
    private String hostServerName;

    @Column(name = "is_synced", nullable = false, columnDefinition = "TINYINT(1) default 0")
    @Comment("是否已经同步标识：0-未同步；1-已同步")
    private Boolean synced;

    public ChapterSync(Long id, Long chapterId, String hostServerName, Boolean synced) {
        this.id = id;
        this.chapterId = chapterId;
        this.hostServerName = hostServerName;
        this.synced = synced;
    }

    public ChapterSync() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public String getHostServerName() {
        return hostServerName;
    }

    public void setHostServerName(String hostServerName) {
        this.hostServerName = hostServerName;
    }

    public Boolean getSynced() {
        return synced;
    }

    public void setSynced(Boolean synced) {
        this.synced = synced;
    }
}