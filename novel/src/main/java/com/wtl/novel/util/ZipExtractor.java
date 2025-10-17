package com.wtl.novel.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.*;

public class ZipExtractor {

    public static boolean extractZip(String zipFilePath, String outputDir) {
        try (ZipFile zipFile = new ZipFile(zipFilePath, StandardCharsets.UTF_8)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // 跳过目录条目
                if (entry.isDirectory()) {
                    continue;
                }

                // 创建目标文件路径
                File outputFile = new File(outputDir, getFileName(entryName));

                // 确保输出目录存在
                File outputDirFile = outputFile.getParentFile();
                if (!outputDirFile.exists() && !outputDirFile.mkdirs()) {
                    throw new IOException("无法创建目录: " + outputDirFile.getAbsolutePath());
                }

                // 解压文件条目
                try (InputStream inputStream = zipFile.getInputStream(entry);
                     FileOutputStream fos = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("解压 ZIP 文件失败: " + e.getMessage());
            return false;
        }
    }

    private static String getFileName(String entryName) {
        // 获取文件名（去除路径部分）
        int lastSlashIndex = entryName.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            return entryName.substring(lastSlashIndex + 1);
        }
        return entryName;
    }

    public static void main(String[] args) {
        // 示例使用
        String zipFilePath = "C:\\Users\\30402\\Desktop\\NTR\\333\\example.zip"; // ZIP 文件路径
        String outputDir = "C:\\Users\\30402\\Documents\\deepseek\\hahha\\extracted"; // 解压目标目录

        boolean success = extractZip(zipFilePath, outputDir);
        if (success) {
            System.out.println("ZIP 文件解压成功！");
        } else {
            System.out.println("ZIP 文件解压失败！");
        }
    }
}