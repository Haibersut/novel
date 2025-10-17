package com.wtl.novel.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtil {
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex + 1) : "";
    }

    // 提取文件名中的特定前缀（假设文件名格式为 chapter_XXXX.txt）
    public static String extractPrefix(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return null;
        }
        // 获取文件名的主干部分（去掉扩展名）
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    // 筛选以特定前缀开头的图片文件
    public static List<File> filterImagesByPrefix(List<File> imageFiles, String prefix) {
        List<File> result = new ArrayList<>();
        if (prefix == null) {
            return result;
        }

        for (File file : imageFiles) {
            String fileName = file.getName();
            if (fileName.startsWith(prefix)) {
                result.add(file);
            }
        }
        return result;
    }

    public static void deleteFolder(File folder) {
        // 如果文件夹不存在，直接返回
        if (!folder.exists()) {
            System.out.println("文件夹不存在: " + folder.getAbsolutePath());
            return;
        }

        // 如果是文件夹，删除其所有内容
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                // 如果是子文件夹，递归删除
                if (file.isDirectory()) {
                    deleteFolder(file);
                }
                // 删除文件或空文件夹
                if (!file.delete()) {
                    System.out.println("无法删除: " + file.getAbsolutePath());
                }
            }
        }

        // 删除空文件夹
        if (!folder.delete()) {
            System.out.println("无法删除文件夹: " + folder.getAbsolutePath());
        } else {
            System.out.println("已删除文件夹: " + folder.getAbsolutePath());
        }
    }


    // 筛选以特定前缀开头的图片文件
    public static List<File> filterImagesByPrefix(File[] imageFiles, String prefix) {
        List<File> result = new ArrayList<>();
        if (prefix == null) {
            return result;
        }

        for (File file : imageFiles) {
            String fileName = file.getName();
            if (fileName.startsWith(prefix)) {
                result.add(file);
            }
        }
        return result;
    }

    // 处理文件名，提取特定部分并去掉前导零
//    原文件名：chapter_0040_001_0001102_000454.jpg
//    处理后的结果：1102_454
    public static String processFileName(String fileName) {
        // 定义正则表达式来匹配文件名中的特定模式
        Pattern pattern = Pattern.compile(".*_(\\d+)_(\\d+)\\..*");
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            // 提取两组数字
            String part1 = matcher.group(1); // 第一组数字（如 01102）
            String part2 = matcher.group(2); // 第二组数字（如 0454）

            // 去掉前导零并拼接
            return part1.replaceFirst("^0+", "") + "_" + part2.replaceFirst("^0+", "");
        } else {
            // 如果文件名不符合预期格式，返回空或默认值
            return "";
        }
    }

    public static String readContent(String filePath) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return content.toString();
    }

    /**
     * 获取指定目录下的一级子txt文件，并按名称排序
     *
     * @param directoryPath 目标目录路径
     * @return 按名称排序的txt文件数组
     */
    public static File[] getSortedTxtFiles(String directoryPath) {
        // 创建File对象表示目标目录
        File directory = new File(directoryPath);

        // 检查目录是否存在且是目录
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("指定路径不存在或不是目录: " + directoryPath);
            return new File[0];
        }

        // 定义文件过滤器，只包含txt文件
        FileFilter txtFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                // 排除目录，只处理文件
                if (file.isDirectory()) {
                    return false;
                }
                // 检查文件扩展名是否为.txt
                String fileName = file.getName().toLowerCase();
                return fileName.endsWith(".txt");
            }
        };

        // 获取目录下所有txt文件的数组
        File[] txtFiles = directory.listFiles(txtFilter);

        // 如果没有任何txt文件，返回空数组
        if (txtFiles == null || txtFiles.length == 0) {
            System.out.println("目录下没有txt文件: " + directoryPath);
            return new File[0];
        }

        // 按文件名称排序（忽略大小写）
        Arrays.sort(txtFiles, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                // 转换为小写进行比较，确保排序不区分大小写
                return file1.getName().compareToIgnoreCase(file2.getName());
            }
        });

        return txtFiles;
    }
}
