package com.wtl.novel.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EpubTools {

    // 辅助类：用于存储图片元数据
    private static class ImageMeta {
        File sourceFile;
        String imgSrc;
        int chapterIndex;
        int position;
    }

//    public static void main(String[] args) {
//        String epubFilePath = "C:\\Users\\30402\\Downloads\\111.epub";
//        String outputDir = "C:\\Users\\30402\\Documents\\deepseek\\hahha\\test2";
//        try {
//            String contentOpfPath = unzipEpub(epubFilePath, outputDir);
//            List<String> chapterBookmarks = getChapterBookmarks(outputDir, contentOpfPath);
//            splitChaptersByBookmarks(outputDir, chapterBookmarks);
//            System.out.println("处理完成");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static void run(String epubFilePath, String outputDir) throws Exception {
        String contentOpfPath = unzipEpub(epubFilePath, outputDir);
        List<String> chapterBookmarks = getChapterBookmarks(outputDir, contentOpfPath);
        splitChaptersByBookmarks(outputDir, chapterBookmarks);
    }

    public static String unzipEpub(String epubFilePath, String outputDir) throws IOException {
        new File(outputDir).mkdirs();
        String contentOpfPath = null;

        try (ZipFile zipFile = new ZipFile(new File(epubFilePath))) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                File file = new File(outputDir, entryName);
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    try (InputStream entryInputStream = zipFile.getInputStream(entry);
                         OutputStream fileOutputStream = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = entryInputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }

                // 尝试查找content.opf路径（如果这是META-INF/container.xml文件）
                if ("META-INF/container.xml".equalsIgnoreCase(entryName)) {
                    Document doc = Jsoup.parse(new File(outputDir + "/" + entryName), "UTF-8");
                    Element rootfileElement = doc.select("rootfile").first();
                    if (rootfileElement != null) {
                        contentOpfPath = rootfileElement.attr("full-path");
                    }
                }
            }
        }

        return contentOpfPath;
    }

    public static List<String> getChapterBookmarks(String outputDir, String contentOpfPath) {
        List<String> bookmarks = new ArrayList<>();
        File contentOpfFile = new File(outputDir, contentOpfPath);
        if (contentOpfFile.exists()) {
            try {
                Document doc = Jsoup.parse(contentOpfFile, "UTF-8");
                Elements itemElements = doc.select("item");
                for (Element itemElement : itemElements) {
                    String mediaType = itemElement.attr("media-type");
                    if (mediaType.equals("application/xhtml+xml")) {
                        String href = itemElement.attr("href");
                        bookmarks.add(href);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bookmarks;
    }

    public static void splitChaptersByBookmarks(String outputDir, List<String> chapterBookmarks) {
        File epubDir = new File(outputDir);
        List<File> htmlFiles = findHtmlFiles(epubDir);

        if (!htmlFiles.isEmpty()) {
            int index = 1;
            for (String chapterBookmark : chapterBookmarks) {
                String targetFileName = getFileName(chapterBookmark);
                List<File> matchedFiles = htmlFiles.stream()
                        .filter(f -> f.getName().equals(targetFileName))
                        .collect(Collectors.toList());

                if (!matchedFiles.isEmpty()) {
                    // 创建content和img文件夹
                    File contentDir = new File(outputDir, "uploadChapter");
                    contentDir.mkdirs();
                    File imgDir = new File(outputDir, "uploadImg");
                    imgDir.mkdirs();

                    processChapter(matchedFiles.get(0), outputDir, index++);
                }
            }
        }
    }

    private static List<File> findHtmlFiles(File dir) {
        List<File> htmlFiles = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    htmlFiles.addAll(findHtmlFiles(file));
                } else {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".html") || name.endsWith(".htm") || name.endsWith(".xhtml")) {
                        htmlFiles.add(file);
                    }
                }
            }
        }
        return htmlFiles;
    }

    private static String getFileName(String path) {
        Matcher m = Pattern.compile("[/\\\\]([^/\\\\]+)$").matcher(path);
        return m.find() ? m.group(1) : path;
    }

    private static void processChapter(File file, String outputDir, int chapterIndex) {
        try {
            Document doc = Jsoup.parse(file, "UTF-8");
            List<String> lines = new ArrayList<>();
            List<ImageMeta> imageMetas = new ArrayList<>();

            // 第一阶段：收集内容和图片元数据
            processContent(doc.body(), lines, imageMetas, file, chapterIndex);

            // 第二阶段：处理图片并生成信息
            List<String> imageInfos = new ArrayList<>();
            handleImages(imageMetas, outputDir, chapterIndex, lines.size(), lines);

            // 写入章节文件到content文件夹
            File contentDir = new File(outputDir, "uploadChapter");
            contentDir.mkdirs();
            writeChapterFile(contentDir.getAbsolutePath(), chapterIndex, lines, imageInfos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processContent(Node node, List<String> lines,
                                       List<ImageMeta> imageMetas,
                                       File sourceFile, int chapterIndex) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).text().trim();
            if (!text.isEmpty()) {
                lines.add(text);
            }
        } else if (node instanceof Element) {
            Element element = (Element) node;

            if (element.tagName().equalsIgnoreCase("img")) {
                ImageMeta meta = new ImageMeta();
                meta.sourceFile = sourceFile;
                meta.imgSrc = element.attr("src");
                meta.chapterIndex = chapterIndex;
                meta.position = lines.size();
                imageMetas.add(meta);
                lines.add("[IMAGE_PLACEHOLDER]");
                return;
            }

            if (element.tagName().equalsIgnoreCase("br")) {
                lines.add("");
            } else if (isBlockElement(element)) {
                if (!lines.isEmpty() && !lines.get(lines.size() - 1).isEmpty()) {
                    lines.add("");
                }
                for (Node child : element.childNodes()) {
                    processContent(child, lines, imageMetas, sourceFile, chapterIndex);
                }
                if (!lines.isEmpty() && !lines.get(lines.size() - 1).isEmpty()) {
                    lines.add("");
                }
            } else {
                for (Node child : element.childNodes()) {
                    processContent(child, lines, imageMetas, sourceFile, chapterIndex);
                }
            }
        }
    }

    private static boolean isBlockElement(Element element) {
        String tag = element.tagName().toLowerCase();
        return tag.matches("div|p|h[1-6]|ul|ol|li|blockquote|section");
    }

    private static void handleImages(List<ImageMeta> metas, String outputDir,
                                     int chapterIndex, int totalLines,
                                     List<String> lines) throws IOException {
        int imageNum = 1;
        for (ImageMeta meta : metas) {
            File imgFile = new File(meta.sourceFile.getParentFile(), meta.imgSrc);
            if (imgFile.exists()) {
                String ext = getFileExtension(meta.imgSrc);
                String newName = String.format("chapter_%04d_%03d_%04d_%04d.%s",
                        chapterIndex,
                        imageNum,
                        meta.position + 1,
                        totalLines,
                        ext);

                // 创建img文件夹并复制图片
                File imgDir = new File(outputDir, "uploadImg");
                imgDir.mkdirs();
                Files.copy(imgFile.toPath(), Paths.get(imgDir.getAbsolutePath(), newName));
                imageNum++;
            }
        }
    }

    private static String getFileExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot == -1 ? "" : filename.substring(dot + 1);
    }

    private static void writeChapterFile(String outputDir, int chapterIndex,
                                         List<String> lines,
                                         List<String> imageInfos) throws IOException {
        String chapterId = String.format("chapter_%04d", chapterIndex);
        File txtFile = new File(outputDir, chapterId + ".txt");

        try (BufferedWriter writer = Files.newBufferedWriter(txtFile.toPath())) {
            writer.write(String.format("=== 第 %04d 章 ===\n", chapterIndex));
            writer.write(String.format("总行数: %d\n\n", lines.size()));

            for (String line : lines) {
                writer.write(line + "\n");
            }

            if (!imageInfos.isEmpty()) {
                writer.write("\n=== 图片信息 ===\n");
                writer.write(String.format("章节: %s\n", chapterId));
                writer.write(String.format("图片总数: %d\n", imageInfos.size()));
                for (String info : imageInfos) {
                    writer.write(info + "\n");
                }
            }
        }
    }
}