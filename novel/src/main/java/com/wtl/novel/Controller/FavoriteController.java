package com.wtl.novel.Controller;

import com.wtl.novel.CDO.FavoriteCTO;
import com.wtl.novel.CDO.ReadingRecordCTO;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.FavoriteService;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.Favorite;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private CredentialService credentialService;



    @GetMapping("/user/{objectId}/{favoriteType}")
    public ResponseEntity<Boolean> existsByUserIdAndObjectIdAndFavoriteType(@PathVariable Long objectId,
    @PathVariable String favoriteType, HttpServletRequest httpRequest) {
        String authorization = httpRequest.getHeader("Authorization");
        if (authorization == null) {
            return ResponseEntity.ok(false);
        }
        String[] authorizationInfo = authorization.split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
        Boolean favorites = favoriteService.existsByUserIdAndObjectIdAndFavoriteType(credential.getUser().getId(), objectId, favoriteType);
        return ResponseEntity.ok(favorites);
    }

    // 获取用户的所有收藏
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Favorite>> getAllFavoritesByUserId(@PathVariable Long userId) {
        List<Favorite> favorites = favoriteService.getAllFavoritesByUserId(userId);
        return ResponseEntity.ok(favorites);
    }

    // 获取用户特定类型的收藏
    @GetMapping("/user/type/{favoriteType}")
    public ResponseEntity<List<FavoriteCTO>> getFavoritesByUserIdAndType(
            @PathVariable String favoriteType, HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ArrayList<>());
        }
        List<FavoriteCTO> favorites = favoriteService.getFavoritesByUserIdAndType(favoriteType, credential.getUser());
        return ResponseEntity.ok(favorites);
    }

    // 获取用户特定类型的收藏
    @GetMapping("/getHistory")
    public ResponseEntity<List<ReadingRecordCTO>> getHistory(HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ArrayList<>());
        }
        List<ReadingRecordCTO> favorites = favoriteService.getHistory(credential.getUser());
        return ResponseEntity.ok(favorites);
    }

    // 获取用户特定类型的收藏
    @GetMapping("/user/group/{groupId}")
    public ResponseEntity<List<FavoriteCTO>> getFavoritesByUserIdAndGroup(
            @PathVariable Long groupId, HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ArrayList<>());
        }
        List<FavoriteCTO> favorites = favoriteService.getFavoritesByUserIdAndGroup(groupId,credential.getUser());
        return ResponseEntity.ok(favorites);
    }
}