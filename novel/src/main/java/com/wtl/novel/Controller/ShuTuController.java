package com.wtl.novel.Controller;

import com.wtl.novel.CDO.*;
import com.wtl.novel.DTO.ChapterProjection;
import com.wtl.novel.DTO.TextNumCount;
import com.wtl.novel.DTO.UserIdUsernameDTO;
import com.wtl.novel.Service.*;
import com.wtl.novel.entity.*;
import com.wtl.novel.entity.Dictionary;
import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.repository.RequestLogRepository;
import com.wtl.novel.repository.UserRepository;
import com.wtl.novel.util.SignatureUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/shuTu")
public class ShuTuController {

    @Autowired
    private ShuTuNovelChapterService shuTuNovelChapterService;
    @Autowired
    private UserService userService;


    private Map<String, String> buildGetMap(int... chapters) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int chapter : chapters) {
            map.put(String.valueOf(chapter), "https://booktoki468.com/novel/xxx/" + chapter);
        }
        return map;
    }


    @GetMapping("/getNoDownloadInfo")
    public Page<BookMetaDto> getNoDownloadInfo(HttpServletRequest request,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        User userByToken = userService.getUserByToken(request);
        return shuTuNovelChapterService.getAllNoDownloadInfo(userByToken.getId(), page, size);
    }

    @GetMapping("/getAllNoDownloadInfo")
    public Page<BookMetaDto> getAllNoDownloadInfo(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return shuTuNovelChapterService.getAllNoDownloadInfo(page, size);
    }

    @PostMapping("/searchNoDownloadInfo")
    public Page<BookMetaDto> searchNoDownloadInfo(@RequestBody BookInfo bookInfo,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return shuTuNovelChapterService.searchNoDownloadInfo(bookInfo.getBookUrl(),page, size);
    }


//    @GetMapping("/test")
//    public Map<String, Map<String, Map<String, Object>>> test() {
//        /* =========================  我的上传  ========================= */
//        Map<String, Object> book1 = new HashMap<>();
//        book1.put("name", "第一本书");
//        book1.put("url", "https://booktoki468.com/novel/17401138");
//        book1.put("noGet", Arrays.asList(451, 233, 197));
//
//        Map<String, Object> book2 = new HashMap<>();
//        book2.put("name", "第2本书");
//        book2.put("url", "https://booktoki468.com/novel/16924774");
//        book2.put("noGet", Arrays.asList(451, 233, 197));
//
//        Map<String, Object> book3 = new HashMap<>();
//        book3.put("name", "第3本书");
//        book3.put("url", "https://booktoki468.com/novel/108888");
//        book3.put("noGet", Arrays.asList(451, 233, 197));
//
//        Map<String, Map<String, Object>> myUpload = new LinkedHashMap<>();
//        myUpload.put("第一本书ID(11)", book1);
//        myUpload.put("第2本书ID(65)", book2);
//        myUpload.put("第3本书ID(23)", book3);
//
//        /* =========================  书友上传  ========================= */
//        Map<String, Object> book4 = new HashMap<>();
//        book4.put("name", "第4本书");
//        book4.put("url", "https://booktoki468.com/novel/11929725");
//        book4.put("noGet", Arrays.asList(451, 233, 197));
//
//        Map<String, Object> book5 = new HashMap<>();
//        book5.put("name", "第5本书");
//        book5.put("url", "https://booktoki468.com/novel/174011385");
//        book5.put("noGet", Arrays.asList(451, 233, 197));
//
//        Map<String, Object> book6 = new HashMap<>();
//        book6.put("name", "第6本书");
//        book6.put("url", "https://booktoki468.com/novel/174011386");
//        book6.put("noGet", Arrays.asList(451, 233, 197));
//
//        Map<String, Map<String, Object>> friendUpload = new LinkedHashMap<>();
//        friendUpload.put("第4本书ID(114)", book4);
//        friendUpload.put("第5本书ID(655)", book5);
//        friendUpload.put("第6本书ID(236)", book6);
//
//        /* =========================  最终返回  ========================= */
//        Map<String, Map<String, Map<String, Object>>> result = new LinkedHashMap<>();
//        result.put("我的上传", myUpload);
//        result.put("书友上传", friendUpload);
//        return result;
//    }

    @PostMapping("/updateShuTu")
    public Boolean updateShuTu(@RequestBody BookInfo bookInfo, HttpServletRequest httpServletRequest) {
        User userByToken = userService.getUserByToken(httpServletRequest);
        return shuTuNovelChapterService.updateShuTu(bookInfo, userByToken);
    }
    @PostMapping("/saveContent")
    public Boolean saveContent(@RequestBody ChapterContent chapterContent, HttpServletRequest httpServletRequest) {
        User userByToken = userService.getUserByToken(httpServletRequest);
        return shuTuNovelChapterService.saveContent(chapterContent, userByToken);
    }

    @PostMapping("/saveNovel")
    public String saveNovel(@RequestBody ShuTuUploadMetadata metadata, HttpServletRequest httpServletRequest) {
        User userByToken = userService.getUserByToken(httpServletRequest);
        return shuTuNovelChapterService.saveNovel(metadata, userByToken.getId());
    }



}