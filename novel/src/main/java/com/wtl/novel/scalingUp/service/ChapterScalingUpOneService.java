package com.wtl.novel.scalingUp.service;


import com.wtl.novel.scalingUp.entity.ChapterScalingUpOne;
import com.wtl.novel.scalingUp.repository.ChapterScalingUpOneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
public class ChapterScalingUpOneService {

    @Autowired
    private ChapterScalingUpOneRepository chapterRepository;

    public void test () {
        List<ChapterScalingUpOne> all = chapterRepository.findAll();
        System.out.println(1);
    }

}