package com.wtl.novel.Config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.wtl.novel.repository.DictionaryRepository;
import com.wtl.novel.translator.Novelpia;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TaskSchedulerManager {

    @Autowired
    @Qualifier("schedulerExecutor")
    private ScheduledExecutorService schedulerExecutor;

    @Autowired
    @Qualifier("taskExecutor1")
    private ExecutorService taskExecutor1;

    @Autowired
    private Novelpia novelpia;

    @Autowired
    private DictionaryRepository dictionaryRepository;

    private volatile boolean isRunning = false;

    // 启动任务调度
    @PostConstruct // 应用启动后自动运行
    public void startScheduling() {
        if (!isRunning) {
            isRunning = true;
            schedulerExecutor.scheduleAtFixedRate(
                    this::submitTask,
                    0,    // 初始延迟0秒
                    60,   // 每10秒提交一次
                    TimeUnit.SECONDS
            );
        }
    }

    // 提交任务到线程池
    private void submitTask() {
        if (shouldExecute()) { // 检查执行条件（如数据库配置）
            taskExecutor1.submit(this::executeTaskLogic2);
            taskExecutor1.submit(this::executePhoto);
            taskExecutor1.submit(this::executeUploadTranslation);
            taskExecutor1.submit(this::executeUploadTranslationException);
            taskExecutor1.submit(this::executeTaskLogic3);
            taskExecutor1.submit(this::fixErrorChapter);
        }
    }

    // 具体任务逻辑
    private void executeTaskLogic2() {
        novelpia.executeTask2();
    }
    // 具体任务逻辑
    private void executeTaskLogic3() {
        novelpia.executeTask3();
    }
    // 具体任务逻辑
    private void fixErrorChapter() {
        novelpia.fixErrorChapter();
    }
    // 具体任务逻辑
    private void executePhoto() {
        novelpia.photo();
    }
    // 具体任务逻辑
    private void executeUploadTranslation() {
        novelpia.executeUploadTranslation();
    }
    // 具体任务逻辑
    private void executeUploadTranslationException() {
        novelpia.executeUploadTranslationException();
    }

    // 动态控制执行条件（例如从数据库读取）
    private boolean shouldExecute() {


        // 实现你的条件判断逻辑，例如：
        // return Boolean.parseBoolean(dictionaryRepository.findByKey("executeTr").getValue());
        return Boolean.parseBoolean(dictionaryRepository.findDictionaryByKeyFieldAndIsDeletedFalse("controllAllTr").getValueField());
    }

    // 停止调度（可选）
    public void stopScheduling() {
        isRunning = false;
        schedulerExecutor.shutdown();
    }
}