package com.wtl.novel.Controller;

import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.UserTagFilterService;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.UserTagFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tag-filters")
public class UserTagFilterController {

    private final UserTagFilterService service;

    private final CredentialService credentialService;

    public UserTagFilterController(UserTagFilterService service, CredentialService credentialService) {
        this.service = service;
        this.credentialService = credentialService;
    }

    /**
     * 给用户添加过滤标签
     * POST /api/v1/tag-filters?userId=1&tagId=10
     */
    @GetMapping("/getFilterTag")
    public ResponseEntity<List<UserTagFilter>> getFilterTag(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            return ResponseEntity.status(401).body(null);
        }
        String[] authorizationInfo = authHeader.split(";");
        if (authorizationInfo.length == 0) {
            return ResponseEntity.status(401).body(null);
        }
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getUser() == null) {
            return ResponseEntity.status(401).body(null);
        }
        List<UserTagFilter> userTagFilters = service.getFilterTag(credential.getUser().getId());
        return ResponseEntity.ok(userTagFilters);
    }

    @GetMapping("/filterTag/{id}")
    public void filterTag(@PathVariable("id") Long id,
                                                         HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            return;
        }
        String[] authorizationInfo = authHeader.split(";");
        if (authorizationInfo.length == 0) {
            return;
        }
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getUser() == null) {
            return;
        }
        service.filterTag(id,credential.getUser().getId());
    }
}