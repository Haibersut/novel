package com.wtl.novel.util;

import java.io.File;

import java.io.File;

public class DiskSpaceUtils {

    /**
     * 检查指定路径所在磁盘的剩余空间是否不足3GB
     * @return true-空间不足3GB，false-空间充足
     */
    public static boolean isSpaceLessThan3GB() {
        // Windows示例路径（检查D盘）
        String winPath = "C:\\";
        // Linux示例路径（检查根分区）
        String linuxPath = "/home";

        File file = new File(System.getProperty("os.name").toLowerCase().contains("win") ? winPath : linuxPath);
        if (!file.exists()) {
            throw new IllegalArgumentException("路径不存在");
        }

        // 获取剩余空间（字节）
        long freeSpaceBytes = file.getFreeSpace();
        // 转换为GB（1GB = 1024^3 bytes）
        long freeSpaceGB = freeSpaceBytes / (1024L * 1024L * 1024L);

        return freeSpaceGB < 1;
    }

}