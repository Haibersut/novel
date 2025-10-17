package com.wtl.novel.entity;

import com.wtl.novel.CDO.BookInfo;
import jakarta.persistence.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "novel_chapter")
public class ShuTuNovelChapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键")
    private Long id;

    @Column(name = "novel_id", nullable = false, updatable = false)
    @Comment("小说ID")
    private Long novelId;

    @Column(name = "chapter_num", nullable = false, updatable = false)
    @Comment("章节序号（第几章）")
    private Integer chapterNum;

    @Column(name = "chapter_true_id", nullable = false, updatable = false, length = 64)
    @Comment("章节真实业务ID（如第三方回传ID）")
    private String chapterTrueId;

    @Column(name = "platform_id", nullable = false, updatable = false)
    @Comment("平台ID（1=起点，2=纵横…）")
    private Integer platformId;

    @Column(name = "downloaded", nullable = false, columnDefinition = "TINYINT(1) UNSIGNED DEFAULT 0")
    @Comment("是否已下载（0=未下载，1=已下载）")
    private Boolean downloaded = false;

    public ShuTuNovelChapter() {
    }


    public ShuTuNovelChapter(BookInfo.ChapterInfo chapter) {
    }

    public Boolean getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(Boolean downloaded) {
        this.downloaded = downloaded;
    }

    public ShuTuNovelChapter(BookInfo.ChapterInfo chapter, Long id, Long id1) {
        this.novelId = id;
        this.chapterNum = chapter.getChapterNum();
        this.chapterTrueId = chapter.getChapterUrl();
        this.platformId = Math.toIntExact(id1);
        this.downloaded = false;
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

    public Integer getChapterNum() {
        return chapterNum;
    }

    public void setChapterNum(Integer chapterNum) {
        this.chapterNum = chapterNum;
    }

    public String getChapterTrueId() {
        return chapterTrueId;
    }

    public void setChapterTrueId(String chapterTrueId) {
        this.chapterTrueId = chapterTrueId;
    }

    public Integer getPlatformId() {
        return platformId;
    }

    public void setPlatformId(Integer platformId) {
        this.platformId = platformId;
    }
}