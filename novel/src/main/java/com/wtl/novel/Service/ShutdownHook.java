package com.wtl.novel.Service;

import com.wtl.novel.repository.ChapterExecuteRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

@Component
public class ShutdownHook {

    @Autowired
    private ChapterExecuteRepository chapterExecuteRepository;

    @PostConstruct
    public void init() {
        // 注册关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                // 执行清理逻辑
                chapterExecuteRepository.updateNowStateToZero();
                System.out.println("Service crashed. now_state=3 records have been updated to 0.");
            } catch (Exception e) {
                System.err.println("Error during shutdown hook execution: " + e.getMessage());
            }
        }));
    }
}