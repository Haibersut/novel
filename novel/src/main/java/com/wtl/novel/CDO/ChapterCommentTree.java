package com.wtl.novel.CDO;

import com.wtl.novel.entity.ChapterComment;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

public class ChapterCommentTree extends ChapterComment {


    @Transient // 标记为非持久化属性
    private List<ChapterCommentTree> children = new ArrayList<>();

    public ChapterCommentTree(ChapterComment comment) {
        // 使用 setter 方法设置属性值
        setId(comment.getId());
        setUsername(comment.getUsername());
        setUserId(comment.getUserId());
        setAvatar(comment.getAvatar());
        setContent(comment.getContent());
        setLikes(comment.getLikes());
        setParentId(comment.getParentId());
        setReplyTo(comment.getReplyTo());
        setCreatedAt(comment.getCreatedAt());
    }

    public List<ChapterCommentTree> getChildren() {
        return children;
    }

    public void setChildren(List<ChapterCommentTree> children) {
        this.children = children;
    }

    // 添加子评论
    public void addChild(ChapterCommentTree child) {
        this.children.add(child);
    }
}