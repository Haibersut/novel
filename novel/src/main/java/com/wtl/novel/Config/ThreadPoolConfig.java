package com.wtl.novel.Config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadPoolConfig {

    // 任务执行线程池（处理具体任务）
    @Bean(name = "taskExecutor1")
    public ExecutorService taskExecutor1() {
        return Executors.newFixedThreadPool(14);
    }

    // 调度线程池（负责定时提交任务）
    @Bean(name = "schedulerExecutor")
    public ScheduledExecutorService schedulerExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    // 应用关闭时关闭线程池
    @PreDestroy
    public void destroy() {
        ((ExecutorService) taskExecutor1()).shutdownNow();
        schedulerExecutor().shutdownNow();
    }
}