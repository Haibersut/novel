package com.wtl.novel.Controller;


import com.wtl.novel.DTO.ChapterSimpleInfo;
import com.wtl.novel.Service.ChapterExecuteService;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.UserNovelRelationService;
import com.wtl.novel.entity.ChapterExecute;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.UserNovelRelation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chaptersExecute")
public class ChapterExecuteController {
    @Autowired
    private ChapterExecuteService chapterExecuteService;
    @Autowired
    private CredentialService credentialService;

    @Autowired
    private UserNovelRelationService userNovelRelationService;

    @GetMapping("/novel/{novelId}")
    public List<ChapterSimpleInfo> getChaptersByNovelId(@PathVariable Long novelId) {
        return chapterExecuteService.findTitlesByNovelIdAndIsDeletedFalseAndNowStateNot(novelId);
    }

    @GetMapping("/getUploadChaptersByNovelId/novel/{novelId}")
    public ResponseEntity<List<ChapterExecute>> getUploadChaptersByNovelId(@PathVariable Long novelId, HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(500).body(null);
        }
        List<UserNovelRelation> upload = userNovelRelationService.getNovelDetail(novelId, "upload", credential.getUser().getId());
        if (upload.isEmpty()) {
            return ResponseEntity.status(500).body(null);
        }
        return ResponseEntity.ok(chapterExecuteService.getChapterExecutesByNovelId(novelId));
    }
}
