package com.wtl.novel.entity;


import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "user_glossary",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_novel_source",
                columnNames = {"novelId", "source_name"}
        ),
        indexes = {
                @Index(name = "idx_user", columnList = "userId"),
                @Index(name = "idx_novel", columnList = "novelId"),
                @Index(name = "idx_source", columnList = "source_name")
        }
)
@EntityListeners(AuditingEntityListener.class)
public class UserGlossary {
    public UserGlossary(Long id, Long userId, Long novelId, String sourceName, String targetName) {
        this.id = id;
        this.userId = userId;
        this.novelId = novelId;
        this.sourceName = sourceName;
        this.targetName = targetName;
    }

    public UserGlossary() {
    }
    @Transient
    private Integer statue = 0;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long novelId;

    @Column(nullable = false, length = 255)
    private String sourceName;

    @Column(nullable = false, length = 255)
    private String targetName;
    @Transient
    private String modifyName = "";


    public Integer getStatue() {
        return statue;
    }

    public void setStatue(Integer statue) {
        this.statue = statue;
    }

    public String getModifyName() {
        return modifyName;
    }

    public void setModifyName(String modifyName) {
        this.modifyName = modifyName;
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

    public Long getNovelId() {
        return novelId;
    }

    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
}