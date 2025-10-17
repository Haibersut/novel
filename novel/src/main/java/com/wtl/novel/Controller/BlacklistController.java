package com.wtl.novel.Controller;

import com.wtl.novel.Service.BlacklistService;
import com.wtl.novel.Service.UserService;
import com.wtl.novel.entity.User;
import com.wtl.novel.entity.UserBlacklist;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blacklist")
public class BlacklistController {

    private final BlacklistService service;
    private final UserService userService;

    public BlacklistController(BlacklistService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    // 拉黑
    @PostMapping
    public ResponseEntity<UserBlacklist> block(@RequestParam Long blockedId,
                                               HttpServletRequest httpRequest) {
        User userByToken = userService.getUserByToken(httpRequest);
        return ResponseEntity.ok(service.block(userByToken.getId(), blockedId));
    }

    // 取消拉黑
    @DeleteMapping
    public ResponseEntity<Void> unblock(@RequestParam Long blockedId,
                                        HttpServletRequest httpRequest) {
        User userByToken = userService.getUserByToken(httpRequest);
        service.unblock(userByToken.getId(), blockedId);
        return ResponseEntity.noContent().build();
    }

    // 拉黑列表
    @GetMapping("/list")
    public ResponseEntity<Page<UserBlacklist>> list(Pageable pageable,
                                                    HttpServletRequest httpRequest) {
        User userByToken = userService.getUserByToken(httpRequest);
        return ResponseEntity.ok(service.list(userByToken.getId(), pageable));
    }
}