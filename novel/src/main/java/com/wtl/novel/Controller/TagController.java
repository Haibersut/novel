package com.wtl.novel.Controller;

import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.TagService;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tag")
public class TagController {

    @Autowired
    private TagService tagService;
    @Autowired
    private CredentialService credentialService;

    @GetMapping("/all")
    public List<Tag> getAllTags() {
        return tagService.getAllTags();
    }

//    ===
    @GetMapping("/all/{platform}")
    public List<Tag> getAllTags(@PathVariable String platform) {
        return tagService.getAllTagsByPlatform(platform);
    }

    @GetMapping("/allByKeyword/{keyword}")
    public List<Tag> allByKeyword(@PathVariable String keyword) {
        return tagService.allByKeyword(keyword);
    }

    @GetMapping("/allByKeywordBugNoFilter/{keyword}")
    public List<Tag> allByKeywordBugNoFilter(@PathVariable String keyword, HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        return tagService.allByKeywordBugNoFilter(keyword, credential.getUser().getId());
    }


    @GetMapping("/all/{platform}/{keyword}")
    public List<Tag> findByPlatformAnd(@PathVariable String platform, @PathVariable String keyword) {
        return tagService.findByPlatformAnd(platform,keyword);
    }
    @GetMapping("/getTagsByNovelId/{novelId}")
    public List<String> getTagsByNovelId(@PathVariable Long novelId) {
        return tagService.getTagsByNovelId(novelId);
    }
    @GetMapping("/getTagsAllInfoByNovelId/{novelId}")
    public List<Tag> getTagsAllInfoByNovelId(@PathVariable Long novelId) {
        return tagService.getTagsAllInfoByNovelId(novelId);
    }

}
