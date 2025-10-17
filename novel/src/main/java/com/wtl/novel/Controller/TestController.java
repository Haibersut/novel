package com.wtl.novel.Controller;

import com.wtl.novel.Service.TagService;
import com.wtl.novel.entity.Tag;
import com.wtl.novel.scalingUp.service.ChapterScalingUpOneService;
import com.wtl.novel.translator.Novelpia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private Novelpia novelpia;
    @Autowired
    private ChapterScalingUpOneService chapterService;
//
//    @GetMapping("/test1")
//    public boolean getAllTags1() {
//        novelpia.executeDownload();
//        return true;
//    }
//
//    @GetMapping("/test2")
//    public boolean getAllTags2() {
//        novelpia.executeTask2();
//        return true;
//    }


    @GetMapping("/test3")
    public boolean getAllTags3() {
        chapterService.test();
        return true;
    }


}