package com.wtl.novel.Controller;

import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.FavoriteGroupsService;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.FavoriteGroups;
import com.wtl.novel.repository.FavoriteGroupsRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/favoriteGroups")
public class FavoriteGroupsController {
    @Autowired
    private CredentialService credentialService;
    @Autowired
    private FavoriteGroupsService favoriteGroupsService;

    // 创建分组
    @PostMapping("/createFavoriteGroup")
    public ResponseEntity<FavoriteGroups> createFavoriteGroup(@RequestParam String name, HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.ok(null);
        }
        FavoriteGroups createdGroup = favoriteGroupsService.createFavoriteGroup(credential.getUser().getId(), name);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    // 创建分组
    @PostMapping("/update/{groupId}")
    public ResponseEntity<FavoriteGroups> update(@PathVariable Long groupId,
                                                 @RequestParam String name,
                                                 HttpServletRequest httpRequest) {
        if (groupId == 0L || name.length() > 255) {
            return null;
        }
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.ok(null);
        }
        FavoriteGroups createdGroup = favoriteGroupsService.updateFavoriteGroup(groupId, credential.getUser().getId(), name);
        return ResponseEntity.status(HttpStatus.OK).body(createdGroup);
    }


    // 获取用户的所有分组
    @GetMapping("/getAllFavoriteGroups")
    public ResponseEntity<List<FavoriteGroups>> getAllFavoriteGroups(HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.ok(null);
        }
        List<FavoriteGroups> favoriteGroups = favoriteGroupsService.getAllFavoriteGroups(credential.getUser().getId());
        return ResponseEntity.ok(favoriteGroups);
    }

    // 删除分组
    @DeleteMapping("/delete/{groupId}")
    public ResponseEntity<Boolean> deleteFavoriteGroup(@PathVariable Long groupId, HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.ok(null);
        }
        favoriteGroupsService.deleteFavoriteGroup(groupId, credential.getUser().getId());
        return ResponseEntity.ok(true);
    }

}
