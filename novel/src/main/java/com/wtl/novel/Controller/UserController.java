package com.wtl.novel.Controller;


import com.wtl.novel.CDO.LoginRequestCTO;
import com.wtl.novel.CDO.UserCTO;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.InvitationCodeService;
import com.wtl.novel.Service.UserService;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.InvitationCode;
import com.wtl.novel.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private CredentialService credentialService;

    @Autowired
    private InvitationCodeService invitationCodeService;

    @GetMapping("/createInvitationCode")
    public ResponseEntity<InvitationCode> createInvitationCode(HttpServletRequest httpServletRequest) {
        Credential credential = userService.getCredentialByToken(httpServletRequest);
        InvitationCode code = invitationCodeService.createInvitationCode(credential);
        return ResponseEntity.ok(code);
    }

    @GetMapping("/getUserDetail")
    public UserCTO getUserDetail(HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        return new UserCTO(credential.getUser());
    }

    @PostMapping("/saveUserDetail")
    public UserCTO saveUserDetail(@RequestBody UserCTO userCTO,
                              HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        User user = credential.getUser();
        user.setHideReadBooks(userCTO.getHideReadBooks());
        userService.saveUser(user);
        return new UserCTO(credential.getUser());
    }

    @PostMapping("/rename")
    public String rename(@RequestBody LoginRequestCTO request,
                         HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        return userService.rename(request.getEmail(),credential.getUser().getId());
    }


    @GetMapping("/getPoint")
    public Long getPoint( HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        return userService.getUserPoint(credential.getUser().getId());
    }

    @PostMapping("/modifyPassword")
    public int modifyPassword(@RequestBody LoginRequestCTO request,
                                  HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        return userService.modifyPassword(request.getPassword(), credential.getUser());
    }

//    @GetMapping("/geneCode")
//    public String geneCode( HttpServletRequest httpRequest) {
//        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
//        String authorizationHeader = authorizationInfo[0];
//        Credential credential = credentialService.findByToken(authorizationHeader);
//        return invitationCodeService.createOrGetInvitationCode(credential.getUser().getId()).getCode();
//    }

    @GetMapping("/getCode")
    public List<InvitationCode> getCode( HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        return invitationCodeService.getInvitationCode(credential.getUser().getId());
    }

    @GetMapping("/rewardsPoint/{postId}/{points}")
    public ResponseEntity<String> rewardPoints(
            @PathVariable Long postId,
            @PathVariable Long points,
            HttpServletRequest httpRequest) {
        try {
            String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
            String authorizationHeader = authorizationInfo[0];
            Credential credential = credentialService.findByToken(authorizationHeader);
            // 调用服务层处理打赏逻辑
            boolean success = userService.rewardPoints(postId, credential.getUser().getId(), points);
            if (success) {
                return ResponseEntity.ok("打赏成功");
            } else {
                return ResponseEntity.badRequest().body("打赏失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("打赏失败: " + e.getMessage());
        }
    }
}