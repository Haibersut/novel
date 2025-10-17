package com.wtl.novel.Controller;

import com.wtl.novel.Service.CommentService;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.entity.Comment;
import com.wtl.novel.entity.Credential;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private CredentialService credentialService;

    // 添加评论
    @PostMapping("/add")
    public ResponseEntity<String> addComment(@RequestBody Comment comment, HttpServletRequest httpRequest) {
        if (comment.getContent().length() > 20000) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("评论提交失败");
        }
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("评论提交失败");
        }
        comment.setUserId(credential.getUser().getId());
        Comment comment1 = commentService.addComment(comment);
        return ResponseEntity.status(comment1==null ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(comment1==null ? "评论提交失败" : "评论提交成功");
    }

    // 查询章节评论
    @GetMapping("/get/{chapterId}/{page}/{size}")
    public Page<Comment> getCommentsByChapterId(@PathVariable Long chapterId,@PathVariable int page, @PathVariable int size) {
        Pageable pageable = PageRequest.of(page, size);
        return commentService.getCommentsByChapterId(chapterId, pageable);
    }
}