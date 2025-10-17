package com.wtl.novel.Service;

import com.wtl.novel.repository.ChapterExecuteRepository;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CleanupService {

    @Autowired
    private ChapterExecuteRepository chapterExecuteRepository;

    @PreDestroy
    public void cleanup() {
        // 在服务结束时执行逻辑
        chapterExecuteRepository.updateNowStateToZero();
        System.out.println("Service is shutting down. now_state=3 records have been updated to 0.");
    }
}