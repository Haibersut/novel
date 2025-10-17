package com.wtl.novel.Controller;


import com.wtl.novel.CDO.CommentTree;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.PostCommentService;
import com.wtl.novel.Service.UserService;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.PostComment;
import com.wtl.novel.entity.User;
import com.wtl.novel.repository.DictionaryRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostCommentController {

    @Autowired
    private PostCommentService postCommentService;
    @Autowired
    private CredentialService credentialService;
    @Autowired
    private UserService userService;
    @Autowired
    private DictionaryRepository dictionaryRepository;

    @PostMapping("/{postId}/comments")
    public ResponseEntity<PostComment> createComment(@PathVariable Long postId, @RequestBody PostComment comment, HttpServletRequest httpRequest) {
        if (comment.getContent().length() > 3000) {
            return ResponseEntity.status(201).body(new PostComment());
        }
        final boolean replies = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("replies").getValueField());
        if (!replies) {
            return ResponseEntity.status(201).body(new PostComment());
        }
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        comment.setPostId(postId);
        comment.setUsername(credential.getUser().getEmail());
        comment.setUserId(credential.getUser().getId());
        PostComment createdComment = postCommentService.createComment(comment);
        return ResponseEntity.status(201).body(createdComment);
    }

    @GetMapping("/{postId}/comments")
    public List<CommentTree> getAllCommentsByPostId(@PathVariable Long postId,
                                                    HttpServletRequest httpRequest) {
        User userByToken = userService.getUserByToken(httpRequest);
        return postCommentService.getCommentTreeByPostId(userByToken.getId(),postId);
    }


    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<PostComment> replyToComment(@PathVariable Long commentId, @RequestBody PostComment reply,HttpServletRequest httpRequest) {
        if (reply.getContent().length() > 3000) {
            return ResponseEntity.status(201).body(new PostComment());
        }
        final boolean replies = Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("replies").getValueField());
        if (!replies) {
            return ResponseEntity.status(201).body(new PostComment());
        }
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        reply.setUsername(credential.getUser().getEmail());
        reply.setUserId(credential.getUser().getId());
        reply.setParentId(commentId);
        PostComment createdReply = postCommentService.createComment(reply);
        return ResponseEntity.status(201).body(createdReply);
    }

    @GetMapping("/comments/{commentId}/replies")
    public List<PostComment> getRepliesByCommentId(@PathVariable Long commentId) {
        return postCommentService.getRepliesByCommentId(commentId);
    }
}