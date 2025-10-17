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
        String[] authorizationInfo = request.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        List<UserTagFilter> userTagFilters = service.getFilterTag(credential.getUser().getId());
        return ResponseEntity.ok(userTagFilters);
    }

    @GetMapping("/filterTag/{id}")
    public void filterTag(@PathVariable("id") Long id,
                                                         HttpServletRequest request) {
        String[] authorizationInfo = request.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        service.filterTag(id,credential.getUser().getId());
    }
}