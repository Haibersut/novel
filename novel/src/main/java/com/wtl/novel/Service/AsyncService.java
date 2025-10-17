package com.wtl.novel.Service;

import com.wtl.novel.CDO.SyosetuNovelDetail;
import com.wtl.novel.entity.ChapterExecute;
import com.wtl.novel.entity.Novel;
import com.wtl.novel.translator.Novelpia;
import com.wtl.novel.translator.Syosetu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsyncService {

    @Autowired
    private Syosetu syosetu;

    @Autowired
    private Novelpia novelpia;


    @Async
    public void saveNovelAsync(String keyword, SyosetuNovelDetail syosetuNovelDetail, Novel save) {
        try {
            syosetu.saveNovel(keyword, syosetuNovelDetail, save);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void saveNovelPiaAsync(String keyword, SyosetuNovelDetail syosetuNovelDetail, Novel save) {
        try {
            novelpia.saveNovel(keyword, syosetuNovelDetail, save);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}