package com.wtl.novel.Controller;

import com.wtl.novel.CDO.ChapterCommentTree;
import com.wtl.novel.CDO.CommentTree;
import com.wtl.novel.DTO.TextNumCount;
import com.wtl.novel.Service.ChapterCommentService;
import com.wtl.novel.Service.UserService;
import com.wtl.novel.entity.*;
import com.wtl.novel.repository.DictionaryRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chapterComment")
public class ChapterCommentController {

    private final ChapterCommentService chapterCommentService;
    private final DictionaryRepository dictionaryRepository;
    private final UserService userService;

    public ChapterCommentController(ChapterCommentService service, DictionaryRepository dictionaryRepository, UserService userService) {
        this.chapterCommentService = service;
        this.dictionaryRepository = dictionaryRepository;
        this.userService = userService;
    }

    @PostMapping("/comments")
    public ResponseEntity<ChapterComment> createComment(@RequestBody ChapterComment comment, HttpServletRequest httpRequest) {
        if (comment.getContent().length() > 3000) {
            return ResponseEntity.status(201).body(new ChapterComment());
        }
        final boolean replies = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("replies").getValueField());
        if (!replies) {
            return ResponseEntity.status(201).body(new ChapterComment());
        }
        User userByToken = userService.getUserByToken(httpRequest);
        comment.setUsername(userByToken.getEmail());
        comment.setUserId(userByToken.getId());
        ChapterComment createdComment = chapterCommentService.createComment(comment);
        return ResponseEntity.status(201).body(createdComment);
    }

    @GetMapping("/{chapterId}/{textNum}/comments")
    public List<ChapterCommentTree> getAllCommentsByPostId(@PathVariable Long chapterId,
                                                           @PathVariable Integer textNum,
                                                           HttpServletRequest httpRequest) {
        User userByToken = userService.getUserByToken(httpRequest);
        return chapterCommentService.getCommentTreeByPostId(userByToken.getId(),chapterId, textNum);
    }


    @GetMapping("/comments/{chapterId}")
    public List<TextNumCount> countByChapterIdGroupByTextNum(@PathVariable("chapterId") Long chapterId) {
        return chapterCommentService.countByChapterIdGroupByTextNum(chapterId);
    }


    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<ChapterComment> replyToComment(@PathVariable Long commentId, @RequestBody ChapterComment reply,HttpServletRequest httpRequest) {
        if (reply.getContent().length() > 3000) {
            return ResponseEntity.status(201).body(new ChapterComment());
        }
        final boolean replies = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("replies").getValueField());
        if (!replies) {
            return ResponseEntity.status(201).body(new ChapterComment());
        }
        User userByToken = userService.getUserByToken(httpRequest);
        reply.setUsername(userByToken.getEmail());
        reply.setUserId(userByToken.getId());
        reply.setParentId(commentId);
        ChapterComment createdReply = chapterCommentService.createComment(reply);
        return ResponseEntity.status(201).body(createdReply);
    }

    @GetMapping("/comments/{commentId}/replies")
    public List<ChapterComment> getRepliesByCommentId(@PathVariable Long commentId) {
        return chapterCommentService.getRepliesByCommentId(commentId);
    }

}