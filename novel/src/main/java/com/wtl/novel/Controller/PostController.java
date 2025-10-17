package com.wtl.novel.Controller;

import com.wtl.novel.CDO.PostCTO;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.PostAgreeService;
import com.wtl.novel.Service.PostService;
import com.wtl.novel.Service.UserService;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.Post;
import com.wtl.novel.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private UserService userService;
    @Autowired
    private CredentialService credentialService;
    @Autowired
    private PostAgreeService postAgreeService;
    private final ConcurrentMap<Long, Object> userLocks = new ConcurrentHashMap<>();


    @PostMapping("/agreeApiVersion1/{postId}/{agree}")
    public void agree(@PathVariable Long postId,
                      @PathVariable String agree,
                      HttpServletRequest httpRequest) {

        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        Long userId = credential.getUser().getId();
        Object lock = userLocks.computeIfAbsent(userId, k -> new Object());

        synchronized (lock) {
            try {
                postAgreeService.agree(postId, userId, agree);
            } finally {
                userLocks.remove(userId);
            }
        }
    }

    @GetMapping("/deletePost/{id}")
    public ResponseEntity<Boolean> deletePost(
            @PathVariable("id") Long id,
            HttpServletRequest httpRequest) {
        User userByToken = userService.getUserByToken(httpRequest);
        postService.deletePost(id,userByToken.getId());
        return ResponseEntity.ok(true);
    }

    @GetMapping("/getPostsByUserId")
    public ResponseEntity<Page<PostCTO>> getPostsByUserId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            HttpServletRequest httpRequest) {

        if (size > 50) {
            return null;
        }
        // 创建分页和排序对象
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.Direction.fromString(sortDirection),
                sortBy
        );
        User userByToken = userService.getUserByToken(httpRequest);
        return ResponseEntity.ok(postService.getPostsByUserId(userByToken.getId(), pageable));
    }

    @GetMapping("/getPosts")
    public ResponseEntity<Page<PostCTO>> getAllPosts(
            @RequestParam(required = false) Integer postType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            HttpServletRequest httpRequest) {
        if (size > 50) {
            return null;
        }
        // 创建分页和排序对象
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.Direction.fromString(sortDirection),
                sortBy
        );
        User userByToken = userService.getUserByToken(httpRequest);
        return ResponseEntity.ok(postService.getPostsByPostType(userByToken.getId(),postType, pageable));
    }

    @GetMapping("/getAllPostsByNovelId")
    public ResponseEntity<Page<PostCTO>> getAllPostsByNovelId(
            @RequestParam Long novelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        if (size > 50) {
            return null;
        }
        // 创建分页和排序对象
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.Direction.fromString(sortDirection),
                sortBy
        );

        return ResponseEntity.ok(postService.getAllPostsByNovelId(novelId, pageable));
    }



    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id, HttpServletRequest httpRequest) {
        String authorization = httpRequest.getHeader("Authorization");
        String[] authorizationInfo = authorization.split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        Post post = postService.getPostById(id);
        Integer agree = postAgreeService.getPostAgreeByUserId(id, credential.getUser().getId());
        post.setAgreeUser(agree);
        return ResponseEntity.ok(post);
    }

    @PostMapping("/createPost")
    public ResponseEntity<Post> createPost(@RequestBody Post post, HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        post.setUserId(credential.getUser().getId());
        post.setAuthor(credential.getUser().getEmail());
        post.setCommentNum(0);
        Post createdPost = postService.createPost(post);
        return ResponseEntity.status(201).body(createdPost);
    }

}