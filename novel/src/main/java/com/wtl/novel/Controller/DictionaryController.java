package com.wtl.novel.Controller;


import com.wtl.novel.CDO.LoginRequestCTO;
import com.wtl.novel.CDO.UntilInfo;
import com.wtl.novel.Service.DictionaryService;
import com.wtl.novel.Service.UserOperationLogService;
import com.wtl.novel.Service.UserService;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.Dictionary;
import com.wtl.novel.entity.User;
import com.wtl.novel.util.CloudflareR2Util;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/dic")
public class DictionaryController {

    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private UserOperationLogService userOperationLogService;
    @Autowired
    private UserService userService;
    @Autowired
    private CloudflareR2Util cloudflareR2Util;

    @GetMapping("/getHome")
    public List<Dictionary> getPoint() {
        return dictionaryService.findByKeyFieldLikeAndIsDeletedFalse("/api/dic/getHome%");
    }

    @GetMapping("/findCookie")
    public List<UntilInfo> findCookie() {
        return dictionaryService.findCookie();
    }

    @GetMapping("/deleteCookie/{dicId}")
    public Boolean deleteCookie(@PathVariable("dicId") Long dicId, HttpServletRequest request) {
        User userByToken = userService.getUserByToken(request);
        return dictionaryService.deleteCookie(dicId, userByToken.getId());
    }

    @GetMapping("/findCookieByUserId")
    public List<UntilInfo> findCookieByUserId(HttpServletRequest request) {
        User userByToken = userService.getUserByToken(request);
        return dictionaryService.findCookieByUserId(userByToken.getId());
    }

    @GetMapping("/getFontMapVersion")
    public Integer getFontMapVersion() {
        return Integer.valueOf(dictionaryService.getDictionaryByKey("fontVersion").getValueField());
    }

    @GetMapping("/getFontFile")
    public ResponseEntity<Resource> getFontFile(HttpServletRequest httpRequest) throws IOException {
        User userByToken = userService.getUserByToken(httpRequest);
        if (userByToken == null || userByToken.getId() <= 0L) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (true) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        userOperationLogService.addLog(userByToken.getId(), "getFontFile", "getFontFile");

        String targetDirectoryBasedOnOS = cloudflareR2Util.getTempDirectory();
        File fontFile = new File(targetDirectoryBasedOnOS + "novelFont.ttf");
        if (!fontFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(fontFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("font/ttf"));
        headers.setContentLength(fontFile.length());

        // 禁用缓存
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @PostMapping("/addCookie")
    public String addCookie(@RequestBody UntilInfo untilInfo,
                              HttpServletRequest httpRequest) {
        if (untilInfo.getDeadline().length() > 10000) {
            return "添加失败，当前Cookie格式错误";
        }
        User userByToken = userService.getUserByToken(httpRequest);
        return dictionaryService.addCookie(userByToken,untilInfo);
    }


    @GetMapping("/getNovelDetail")
    public List<Dictionary> getNovelDetail() {
        return dictionaryService.findByKeyFieldLikeAndIsDeletedFalse("/api/dic/getNovelDetail%");
    }

    @GetMapping("/getDicPoint")
    public String getDicPoint() {
        return dictionaryService.getDictionaryByKey("createInvitationCode").getValueField();
    }

    @GetMapping("/getReNamePoint")
    public String getReNamePoint() {
        return dictionaryService.getDictionaryByKey("ReNamePoint").getValueField();
    }

}
