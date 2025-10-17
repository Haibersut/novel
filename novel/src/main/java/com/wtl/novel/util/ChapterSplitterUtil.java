package com.wtl.novel.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class ChapterSplitterUtil {

    private static final Logger log = LoggerFactory.getLogger(ChapterSplitterUtil.class);

    // 配置参数
    private static final int CHAPTER_TARGET_LENGTH = 5000; // 每章节目标字数
    private static final int MAX_CHAPTER_LENGTH = 10000;  // 单章节最大字数（用于进一步分割）

    public static List<String> splitAndSaveChapters(String filePath, String outputDir) {
        // 创建输出目录
        File dir = new File(outputDir);
        if (!dir.exists() && !dir.mkdirs()) {
            log.error("无法创建输出目录: {}", outputDir);
            return Collections.emptyList();
        }

        List<String> allLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Files.newInputStream(Paths.get(filePath)),
                        StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                allLines.add(line);
            }
        } catch (IOException e) {
            log.error("文件读取失败: {}", filePath, e);
            return Collections.emptyList();
        }

        List<Chapter> chapters = new ArrayList<>();
        Chapter currentChapter = null;

        for (int i = 0; i < allLines.size(); ) {
            String line = allLines.get(i).trim();

            // 检测是否为章节标题
            if (isChapterTitle(line)) {
                // 如果当前章节有内容，保存当前章节
                if (currentChapter != null && !currentChapter.getContent().isEmpty()) {
                    chapters.add(currentChapter);
                }
                // 创建新章节
                currentChapter = new Chapter(line);
                i++; // 移动到下一行
            } else {
                // 如果没有检测到章节标题，尝试自动分章
                if (currentChapter == null) {
                    currentChapter = new Chapter("默认章节");
                }
                // 将当前行添加到当前章节
                currentChapter.addContentLine(line);
                i++;
            }
        }

        // 处理最后一章
        if (currentChapter != null && !currentChapter.getContent().isEmpty()) {
            chapters.add(currentChapter);
        }

        // 进一步处理超长章节
        List<Chapter> finalChapters = new ArrayList<>();
        for (Chapter chapter : chapters) {
            if (chapter.getContentLength() > MAX_CHAPTER_LENGTH) {
                // 分割超长章节
                List<Chapter> splitChapters = splitChapter(chapter);
                finalChapters.addAll(splitChapters);
            } else {
                finalChapters.add(chapter);
            }
        }

        // 保存所有章节到文件
        List<String> savedFileNames = new ArrayList<>();
        for (int i = 0; i < finalChapters.size(); i++) {
            Chapter chapter = finalChapters.get(i);
            String fileName = saveChapterToFile(i + 1, chapter, outputDir);
            if (fileName != null) {
                savedFileNames.add(fileName);
            }
        }

        return savedFileNames;
    }

    private static boolean isChapterTitle(String line) {
        return Pattern.matches("^第[一二三四五六七八九十零百]+章(\\s+.*)?$", line) ||
                Pattern.matches("^第\\d+章(\\s+.*)?$", line) ||
                Pattern.matches("^(chapter|Chapter)\\s*\\d+(\\s+.*)?$", line);
    }

    private static List<Chapter> splitChapter(Chapter chapter) {
        List<Chapter> splitChapters = new ArrayList<>();
        List<String> contentLines = chapter.getContentLines();
        int startIndex = 0;

        while (startIndex < contentLines.size()) {
            int currentLength = 0;
            int endIndex = startIndex;
            while (endIndex < contentLines.size() && currentLength < CHAPTER_TARGET_LENGTH) {
                String line = contentLines.get(endIndex);
                if (currentLength + line.length() > CHAPTER_TARGET_LENGTH) {
                    break;
                }
                currentLength += line.length();
                endIndex++;
            }

            // 创建分割后的章节
            Chapter splitChapter = new Chapter(chapter.getTitle() + "_分割部分" + (splitChapters.size() + 1));
            for (int i = startIndex; i < endIndex; i++) {
                splitChapter.addContentLine(contentLines.get(i));
            }
            splitChapters.add(splitChapter);
            startIndex = endIndex;
        }

        return splitChapters;
    }

    private static String saveChapterToFile(int epNumber, Chapter chapter, String outputDir) {
        String safeTitle = chapter.getTitle().replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "_");
        String fileName = String.format("%s/EP%04d_gaga分隔符_%s.txt",
                outputDir,
                epNumber,
                safeTitle.substring(0, Math.min(20, safeTitle.length())));

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        Files.newOutputStream(Paths.get(fileName)),
                        StandardCharsets.UTF_8))) {

            for (String content : chapter.getContentLines()) {
                writer.write(content);
                writer.newLine();
            }

        } catch (IOException e) {
            log.error("保存章节文件失败", e);
            return null;
        }
        return fileName;
    }

    static class Chapter {
        private String title;
        private int startLine;
        private int endLine;
        private List<String> contentLines;
        private int contentLength;

        public Chapter(String title) {
            this.title = title;
            this.startLine = 1; // 默认起始行
            this.contentLines = new ArrayList<>();
            this.contentLength = 0;
        }

        public void addContentLine(String line) {
            contentLines.add(line);
            contentLength += line.length();
        }

        public String getTitle() {
            return title;
        }

        public List<String> getContentLines() {
            return contentLines;
        }

        public int getContentLength() {
            return contentLength;
        }

        public int getStartLine() {
            return startLine;
        }

        public int getEndLine() {
            return endLine;
        }

        public void setStartLine(int startLine) {
            this.startLine = startLine;
        }

        public void setEndLine(int endLine) {
            this.endLine = endLine;
        }

        public void setContentLines(List<String> contentLines) {
            this.contentLines = contentLines;
            this.contentLength = contentLines.stream().mapToInt(String::length).sum();
        }

        public List<String> getContent() {
            return contentLines;
        }
    }
    
}