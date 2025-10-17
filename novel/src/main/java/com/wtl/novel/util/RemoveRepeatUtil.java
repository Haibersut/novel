package com.wtl.novel.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//字符串重复处理器
public class RemoveRepeatUtil {

    // 核心处理方法（接收字符串返回处理结果）
    public static String processString(String input) {
        // 将字符串拆分为行
        List<String> lines = splitIntoLines(input);

        // 处理连续重复行
        List<String> processedLines = processConsecutiveLines(lines);

        // 处理每行的重复子字符串
        for (int i = 0; i < processedLines.size(); i++) {
            String processed = processLine(processedLines.get(i));
            processedLines.set(i, processed);
        }

        // 合并为最终字符串
        return joinLines(processedLines);
    }

    // 字符串拆分行（支持各种换行符）
    private static List<String> splitIntoLines(String input) {
        return Arrays.asList(input.split("\\R")); // 匹配所有换行符
    }

    // 行列表合并为字符串
    private static String joinLines(List<String> lines) {
        return String.join(System.lineSeparator(), lines);
    }

    // 以下原有方法保持不变 ↓↓↓

    // 处理连续重复行（与原代码相同）
    private static List<String> processConsecutiveLines(List<String> lines) {
        List<String> result = new ArrayList<>();
        if (lines.isEmpty()) return result;

        String currentLine = lines.get(0);
        int count = 1;

        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).equals(currentLine)) {
                count++;
            } else {
                addProcessedLines(result, currentLine, count);
                currentLine = lines.get(i);
                count = 1;
            }
        }
        addProcessedLines(result, currentLine, count);
        return result;
    }

    private static void addProcessedLines(List<String> result, String line, int count) {
        if (count >= 4) {
            result.add(line);
            result.add(line);
        } else {
            for (int i = 0; i < count; i++) {
                result.add(line);
            }
        }
    }

    // 处理每行的重复子字符串（与原代码相同）
    private static String processLine(String s) {
        boolean modified;
        do {
            modified = false;
            int maxK = s.length() / 4;

            for (int k = maxK; k >= 1; k--) {
                for (int i = 0; i <= s.length() - 4 * k; i++) {
                    String pattern = s.substring(i, i + k);
                    int repeats = 1;
                    int pos = i + k;

                    while (pos + k <= s.length() && s.substring(pos, pos + k).equals(pattern)) {
                        repeats++;
                        pos += k;
                    }

                    if (repeats >= 4) {
                        StringBuilder sb = new StringBuilder();
                        for (int j = 0; j < 2; j++) {
                            sb.append(pattern);
                        }
                        s = s.substring(0, i) + sb.toString() + s.substring(i + repeats * k);
                        modified = true;
                        k = maxK + 1;
                        break;
                    }
                }
                if (modified) break;
            }
        } while (modified);
        return s;
    }
}
