package com.wtl.novel.CDO;

import com.wtl.novel.entity.PostComment;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

public class CommentTree extends PostComment {


    @Transient // 标记为非持久化属性
    private List<CommentTree> children = new ArrayList<>();

    public CommentTree(PostComment comment) {
        // 使用 setter 方法设置属性值
        setId(comment.getId());
        setPostId(comment.getPostId());
        setUsername(comment.getUsername());
        setUserId(comment.getUserId());
        setAvatar(comment.getAvatar());
        setContent(comment.getContent());
        setLikes(comment.getLikes());
        setParentId(comment.getParentId());
        setReplyTo(comment.getReplyTo());
        setCreatedAt(comment.getCreatedAt());
    }

    public List<CommentTree> getChildren() {
        return children;
    }

    public void setChildren(List<CommentTree> children) {
        this.children = children;
    }

    // 添加子评论
    public void addChild(CommentTree child) {
        this.children.add(child);
    }
}