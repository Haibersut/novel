package com.wtl.novel.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "terminology")
public class Terminology {
    public Terminology() {
    }



    public Terminology(Long novelId,String novelTrueId, String chapterTrueId, String sourceName, String targetName, Integer chapterNum) {
        this.novelId = novelId;
        this.novelTrueId = novelTrueId;
        this.chapterTrueId = chapterTrueId;
        this.sourceName = sourceName;
        this.targetName = targetName;
        this.chapterNum = chapterNum;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "novel_id", nullable = false)
    private Long novelId;

    @Column(name = "novel_true_id")
    private String novelTrueId;

    @Column(name = "chapter_true_id")
    private String chapterTrueId;

    @Column(name = "source_name")
    private String sourceName;

    @Transient
    private String modifyName = "";

    @Transient
    private Integer statue = 0;

    @Column(name = "target_name")
    private String targetName;

    @Column(name = "chapter_num")
    private Integer chapterNum;

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

    public Long getNovelId() {
        return novelId;
    }

    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }


    public String getNovelTrueId() {
        return novelTrueId;
    }

    public void setNovelTrueId(String novelTrueId) {
        this.novelTrueId = novelTrueId;
    }

    public String getChapterTrueId() {
        return chapterTrueId;
    }

    public void setChapterTrueId(String chapterTrueId) {
        this.chapterTrueId = chapterTrueId;
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

    public Integer getChapterNum() {
        return chapterNum;
    }

    public void setChapterNum(Integer chapterNum) {
        this.chapterNum = chapterNum;
    }
}