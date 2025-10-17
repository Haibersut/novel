package com.wtl.novel.Controller;

import com.wtl.novel.CDO.LoginRequestCTO;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.InvitationCodeService;
import com.wtl.novel.Service.UserService;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.InvitationCode;
import com.wtl.novel.entity.User;
import com.wtl.novel.util.CustomPasswordEncoder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private InvitationCodeService invitationCodeService;

    @Autowired
    private CredentialService credentialService;

    @GetMapping("/isLogin")
    public Boolean isLogin(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            return false;
        }
        String[] authorizationInfo = authHeader.split(";");
        if (authorizationInfo.length == 0) {
            return false;
        }
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        return credential != null && !credential.getExpiredAt().isBefore(LocalDateTime.now());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestCTO request) {
        if (request.getEmail().isEmpty()) {
            throw new RuntimeException("用户名格式错误");
        }
        User user = userService.findByEmail(request.getEmail());

        // 用户不存在的情况
        if (user == null) {
            if (!CustomPasswordEncoder.isValid(request.getPassword())) {
                throw new RuntimeException("密码格式错误");
            }
            // 必须提供邀请码才能创建新用户
            if (request.getInvitationCode() == null || request.getInvitationCode().isEmpty()) {
                return ResponseEntity.badRequest().body("首次登录必须提供邀请码/当前用户已存在");
            }

            // 验证邀请码是否有效且未被使用
            InvitationCode invitationCode = invitationCodeService.findByCode(request.getInvitationCode());
            if (invitationCode == null || invitationCode.isUsed()) {
                return ResponseEntity.badRequest().body("无效或已使用的邀请码/当前用户已存在");
            }

            // 创建新用户
            user = userService.createUser(request.getEmail(), request.getPassword());

            // 绑定邀请码
            invitationCodeService.bindToEmail(invitationCode, request.getEmail());
            user.setInvitationCode(invitationCode);
            userService.saveUser(user);
        } else {
            // 用户已存在的情况
            // 如果用户已绑定邀请码，直接验证密码
            if (user.getInvitationCode() != null) {
                if (!userService.checkPassword(user, request.getPassword())) {
                    return ResponseEntity.badRequest().body("密码错误");
                }
            } else {
                // 用户未绑定邀请码，必须提供邀请码
                if (request.getInvitationCode() == null || request.getInvitationCode().isEmpty()) {
                    return ResponseEntity.badRequest().body("首次登录必须提供邀请码/当前用户已存在");
                }

                // 验证邀请码
                InvitationCode invitationCode = invitationCodeService.findByCode(request.getInvitationCode());
                if (invitationCode == null || invitationCode.isUsed()) {
                    return ResponseEntity.badRequest().body("无效或已使用的邀请码/当前用户已存在");
                }

                // 验证密码
                if (!userService.checkPassword(user, request.getPassword())) {
                    return ResponseEntity.badRequest().body("密码错误");
                }

                // 绑定邀请码
                invitationCodeService.bindToEmail(invitationCode, request.getEmail());
                user.setInvitationCode(invitationCode);
                userService.saveUser(user);
            }
        }

        // 生成凭据并返回
        Credential credential = credentialService.generateCredential(user);
        return ResponseEntity.ok(credential.getToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        Credential credential = credentialService.findByToken(token);
        if (credential != null) {
            credentialService.deleteCredential(credential);
            return ResponseEntity.ok("登出成功");
        }
        return ResponseEntity.badRequest().body("无效的凭据");
    }

}