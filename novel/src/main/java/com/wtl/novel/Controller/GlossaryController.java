package com.wtl.novel.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtl.novel.CDO.ChapterRequest;
import com.wtl.novel.CDO.TerminologyModifyCTO;
import com.wtl.novel.CDO.UserTerminologyModifyCTO;
import com.wtl.novel.Service.*;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.Terminology;
import com.wtl.novel.entity.User;
import com.wtl.novel.entity.UserGlossary;
import com.wtl.novel.util.ObfuscateFontOTF;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.wtl.novel.Service.ChapterService.applyFontMapFast;

@RestController
@RequestMapping("/api/glossary")
public class GlossaryController {

    private final UserGlossaryService glossaryService;
    private final UserOperationLogService userOperationLogService;
    private final ChapterService chapterService;
    private final ObfuscateFontOTF obfuscateFontOTF;
    private final CredentialService credentialService;
    private final TerminologyService terminologyService;
    private final UserService userService;

    public GlossaryController(UserGlossaryService glossaryService, UserOperationLogService userOperationLogService, ChapterService chapterService, ObfuscateFontOTF obfuscateFontOTF, CredentialService credentialService, TerminologyService terminologyService, UserService userService) {
        this.glossaryService = glossaryService;
        this.userOperationLogService = userOperationLogService;
        this.chapterService = chapterService;
        this.obfuscateFontOTF = obfuscateFontOTF;
        this.credentialService = credentialService;
        this.terminologyService = terminologyService;
        this.userService = userService;
    }


    @PostMapping("/batchAddFromChapter/{id}")
    public UserGlossary batchAddFromChapter(@PathVariable("id") Long id,@RequestBody ChapterRequest cto,HttpServletRequest request) {
        User userByToken = userService.getUserByToken(request);
        ObjectMapper mapper = new ObjectMapper();
        String ctoJson = null;
        try {
            String title = applyFontMapFast(cto.getTitle(), obfuscateFontOTF.reversedFontMap);
            String content = applyFontMapFast(cto.getContent(), obfuscateFontOTF.reversedFontMap);
            cto.setTitle(title);
            cto.setContent(content);
            ctoJson = mapper.writeValueAsString(cto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        userOperationLogService.addLog(userByToken.getId(),"/api/glossary/batch/" +id,ctoJson);
        return glossaryService.batchSave(cto,userByToken.getId(),id);
    }

    @PostMapping("/batch/{id}")
    public UserGlossary batchAdd(@PathVariable("id") Long id,@RequestBody ChapterRequest cto,HttpServletRequest request) {
        User userByToken = userService.getUserByToken(request);
        ObjectMapper mapper = new ObjectMapper();
        String ctoJson = null;
        try {
            ctoJson = mapper.writeValueAsString(cto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        userOperationLogService.addLog(userByToken.getId(),"/api/glossary/batch/" +id,ctoJson);
        return glossaryService.batchSave(cto,userByToken.getId(),id);
    }



    @GetMapping("/findAllByNovelIdAndStatue/{novelId}/{page}/{size}")
    public Page<UserGlossary> findAllByNovelIdAndStatue(@PathVariable Long novelId,
                                                       @PathVariable Integer page,
                                                       @PathVariable Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return glossaryService.findAllByNovelIdAndStatue(novelId,pageable);
    }

    @GetMapping("/searchAllByNovelIdAndStatue/{novelId}/{page}/{size}")
    public Page<UserGlossary> searchAllByNovelIdAndStatue(@PathVariable Long novelId,
                                                         @RequestParam("keyword") String keyword,
                                                         @PathVariable Integer page,
                                                         @PathVariable Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return glossaryService.findByNovelIdAndKeyword(novelId,pageable, keyword);
    }


    @PostMapping("/processTerminology")
    public boolean processTerminology(
            @RequestBody UserTerminologyModifyCTO cto, HttpServletRequest request) throws JsonProcessingException {
        String[] authorizationInfo = request.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential.getUser().getId() <= 0L) {
            return false;
        }
        // 2. 把 cto 序列化成 JSON 字符串
        ObjectMapper mapper = new ObjectMapper();
        String ctoJson = mapper.writeValueAsString(cto);

        userOperationLogService.addLog(credential.getUser().getId(),"/api/glossary/processTerminology",ctoJson);
        return glossaryService.processTerminology(cto, credential.getUser().getId());
    }
}