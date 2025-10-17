package com.wtl.novel.DTO;

public class PostScoreDto {
    private Integer agree;
    private Integer disagree;
    private Long collections;
    private boolean recommended;

    public PostScoreDto(Integer agree, Integer disagree, Long collections, boolean recommended) {
        this.agree = agree;
        this.disagree = disagree;
        this.collections = collections;
        this.recommended = recommended;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    public Integer getAgree() {
        return agree;
    }

    public void setAgree(Integer agree) {
        this.agree = agree;
    }

    public Integer getDisagree() {
        return disagree;
    }

    public void setDisagree(Integer disagree) {
        this.disagree = disagree;
    }

    public Long getCollections() {
        return collections;
    }

    public void setCollections(Long collections) {
        this.collections = collections;
    }
}