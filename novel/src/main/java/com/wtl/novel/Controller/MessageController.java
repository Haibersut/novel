package com.wtl.novel.Controller;

import com.wtl.novel.Service.MessageService;
import com.wtl.novel.Service.UserService;
import com.wtl.novel.entity.Message;
import com.wtl.novel.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/msg")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    /**
     * 一键全部已读
     */
    @PutMapping("/readAll")
    public ResponseEntity<Void> readAll(HttpServletRequest request) {
        User user = userService.getUserByToken(request);
        messageService.markAllRead(user.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * 单条已读
     */
    @PutMapping("/readOne/{msgId}")
    public ResponseEntity<Void> readOne(@PathVariable Long msgId,
                                        HttpServletRequest request) {
        User user = userService.getUserByToken(request);
        messageService.markOneRead(user.getId(), msgId);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /msg?read=false&page=0&size=20
     * Header 里带登录用户ID（或从 SecurityContext 拿）
     */
    @GetMapping("/getMessage")
    public Page<Message> getMessage(@RequestParam Boolean read,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              HttpServletRequest httpServletRequest) {
        User userByToken = userService.getUserByToken(httpServletRequest);
        return messageService.pageMessage(userByToken.getId(),  read, page, size);
    }

    @GetMapping("/getMessageByType")
    public Page<Message> getMessageByType(@RequestParam Boolean read,
                                    @RequestParam(defaultValue = "") String messageType,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    HttpServletRequest httpServletRequest) {
        User userByToken = userService.getUserByToken(httpServletRequest);
        if (!messageType.isEmpty()) {
            return messageService.pageMessageByType(messageType,userByToken.getId(),  read, page, size);
        }else {
            return messageService.pageMessage(userByToken.getId(),  read, page, size);
        }
    }
}