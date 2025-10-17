package com.wtl.novel.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.*;

@Entity
@Table(name = "novel_tag") // 显式指定表名
public class NovelTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "novel_id") // 显式映射字段名（如果数据库字段名是 novel_id）
    private Long novelId;

    @Column(name = "tag_id") // 显式映射字段名（如果数据库字段名是 tag_id）
    private Long tagId;

    public NovelTag(Long novelId, Long tagId) {
        this.novelId = novelId;
        this.tagId = tagId;
    }

    public NovelTag() {

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

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }
}