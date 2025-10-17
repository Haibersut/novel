package com.wtl.novel.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class LockManager {
    private static final ConcurrentHashMap<String, Object> userLocks = new ConcurrentHashMap<>();

    public static Object getLock(String key) {
        return userLocks.computeIfAbsent(key, k -> new Object());
    }

    // 可选：清理不再使用的锁，防止内存泄漏
    public static void removeLock(String key) {
        userLocks.remove(key);
    }


    private static final ConcurrentHashMap<String, ReentrantLock> LOCKS = new ConcurrentHashMap<>();
    /**
     * 获取锁对象（不存在则新建）
     */
    public static ReentrantLock getTryLock(String key) {
        return LOCKS.computeIfAbsent(key, k -> new ReentrantLock());
    }

}