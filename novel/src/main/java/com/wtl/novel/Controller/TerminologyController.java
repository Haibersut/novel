package com.wtl.novel.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtl.novel.CDO.NovelSearchRequest;
import com.wtl.novel.CDO.TerminologyCTO;
import com.wtl.novel.CDO.TerminologyModifyCTO;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.TerminologyService;
import com.wtl.novel.Service.UserOperationLogService;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.Novel;
import com.wtl.novel.entity.Terminology;
import com.wtl.novel.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/terminologies")
public class TerminologyController {

    @Autowired
    private TerminologyService terminologyService;
    @Autowired
    private CredentialService credentialService;

    @Autowired
    private UserOperationLogService userOperationLogService;


    @GetMapping("/searchAllByNovelIdAndStatue/{novelId}/{page}/{size}")
    public Page<Terminology> searchAllByNovelIdAndStatue(@PathVariable Long novelId,
                                                         @RequestParam("keyword") String keyword,
                                                         @PathVariable Integer page,
                                                         @PathVariable Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return terminologyService.findByNovelIdAndKeyword(novelId,pageable, keyword);
    }

    @GetMapping("/findAllByNovelIdAndStatue/{novelId}/{page}/{size}")
    public Page<Terminology> findAllByNovelIdAndStatue(@PathVariable Long novelId,
                                                       @PathVariable Integer page,
                                                       @PathVariable Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return terminologyService.findAllByNovelIdAndStatue(novelId,pageable);
    }

    @PostMapping("/processTerminology")
    public boolean processTerminology(
            @RequestBody TerminologyModifyCTO cto, HttpServletRequest request) throws JsonProcessingException {
        String[] authorizationInfo = request.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential.getUser().getId() <= 0L) {
            return false;
        }
        // 2. 把 cto 序列化成 JSON 字符串
        ObjectMapper mapper = new ObjectMapper();
        String ctoJson = mapper.writeValueAsString(cto);

        userOperationLogService.addLog(credential.getUser().getId(),"processTerminology",ctoJson);
        return terminologyService.processTerminology(cto, credential.getUser().getId());
    }

    // 根据平台和标签分页查询小说
    @PostMapping("/getTerminologyByPlatform")
    public Page<Terminology> getTerminologyByPlatform(
            @RequestBody TerminologyCTO request) {

        // 创建分页请求
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        // 调用服务层方法
        return terminologyService.getTerminologyByPlatform(
                request.getNovelId(),
                pageable);
    }

}