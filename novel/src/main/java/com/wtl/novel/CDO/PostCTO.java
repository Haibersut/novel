package com.wtl.novel.CDO;

import com.wtl.novel.entity.Post;

public class PostCTO extends Post {
    private String collectionsTitle;
    private Integer agreeUser;

    public PostCTO(Post post, String collectionsTitle) {
        // 使用 setters 将 Post 的值复制到 PostCTO
        setId(post.getId());
        setPostType(post.getPostType());
        setCommentNum(post.getCommentNum());
        setUserId(post.getUserId());
        setTitle(post.getTitle());
        setAuthor(post.getAuthor());
        setContent(post.getContent());
        setCollections(post.getCollections());
        setCreatedAt(post.getCreatedAt());
        setRecommended(post.isRecommended());
        setAgree(post.getAgree());
        setDisagree(post.getDisagree());
        // 设置 collectionsTitle
        this.collectionsTitle = collectionsTitle;
    }

    public Integer getAgreeUser() {
        return agreeUser;
    }

    public void setAgreeUser(Integer agreeUser) {
        this.agreeUser = agreeUser;
    }

    public String getCollectionsTitle() {
        return collectionsTitle;
    }

    public void setCollectionsTitle(String collectionsTitle) {
        this.collectionsTitle = collectionsTitle;
    }
}
