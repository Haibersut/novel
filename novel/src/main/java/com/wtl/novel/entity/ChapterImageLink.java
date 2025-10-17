package com.wtl.novel.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "chapter_image_links")
public class ChapterImageLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chapter_true_id", nullable = false)
    private String chapterTrueId;

    @Column(name = "content_link", nullable = false)
    private String contentLink;

    @Column(name = "location")
    private String location;
    
    @Column(name = "cf")
    private boolean cf;

    public ChapterImageLink() {
    }

    public ChapterImageLink(String chapterTrueId, String contentLink, String location) {
        this.chapterTrueId = chapterTrueId;
        this.contentLink = contentLink;
        this.location = location;
    }

    public boolean isCf() {
        return cf;
    }

    public void setCf(boolean cf) {
        this.cf = cf;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChapterTrueId() {
        return chapterTrueId;
    }

    public void setChapterTrueId(String chapterTrueId) {
        this.chapterTrueId = chapterTrueId;
    }

    public String getContentLink() {
        return contentLink;
    }

    public void setContentLink(String contentLink) {
        this.contentLink = contentLink;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChapterImageLink that = (ChapterImageLink) o;
        return Objects.equals(chapterTrueId, that.chapterTrueId) && Objects.equals(contentLink, that.contentLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chapterTrueId, contentLink);
    }
}