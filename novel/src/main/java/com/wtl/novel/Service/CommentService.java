package com.wtl.novel.Service;


import com.wtl.novel.entity.Comment;
import com.wtl.novel.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    // 添加评论
    public Comment addComment(Comment comment) {
        comment.setIsDeleted(false);
        return commentRepository.save(comment);
    }

    // 查询章节评论
    public Page<Comment> getCommentsByChapterId(Long chapterId, Pageable pageable) {
        return commentRepository.findByChapterIdAndIsDeletedFalse(chapterId, pageable);
    }

    // 删除评论
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("评论不存在"));
        comment.setIsDeleted(true);
        commentRepository.save(comment);
    }
}
