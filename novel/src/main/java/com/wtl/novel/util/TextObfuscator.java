package com.wtl.novel.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TextObfuscator {
    private static final char[] ZERO_WIDTH_CHARS = {
            '\u200B', // 零宽空格
            '\u200C', // 零宽连接符
            '\u200D', // 零宽连接符
            '\uFEFF'  // 零宽非断空格
    };

    // 仅用 ZERO_WIDTH_CHARS 里的字符，两两组合成 10 组
    private static final String[] DIGIT_TO_ZERO_WIDTH = {
            "\u200B\u200B", // 0
            "\u200B\u200C", // 1
            "\u200B\u200D", // 2
            "\u200B\uFEFF", // 3
            "\u200C\u200B", // 4
            "\u200C\u200C", // 5
            "\u200C\u200D", // 6
            "\u200C\uFEFF", // 7
            "\u200D\u200B", // 8
            "\u200D\u200C"  // 9
    };


    private static final Random random = new Random();

    // === 2. 解码函数 ====================================
    /**
     * 从多行文本中提取隐藏数字
     * @param text 已嵌入零宽字符的文本
     * @return 解码后的数字串
     */
    public static String extractNumber(String text) {
        StringBuilder out = new StringBuilder();
        String[] lines = text.split("\n");

        for (String line : lines) {
            int len = line.length();
            if (len < 2) continue;               // 不足 2 字符，跳过
            String tail = line.substring(len - 2); // 取最后 2 字符
            for (int i = 0; i < DIGIT_TO_ZERO_WIDTH.length; i++) {
                if (DIGIT_TO_ZERO_WIDTH[i].equals(tail)) {
                    out.append(i);
                    break;
                }
            }
        }
        return out.toString();
    }

    // === 3. 测试入口 ====================================
    public static void main(String[] args) {
        String sample =
                "看似飘雪般纯白的森林。仔细看便会发现并非积雪覆盖，而是植物与土壤本身皆洁白如雪。" +
                        "\n" +
                        "SgXcFWyOovcgKsjvDJsbaePgMvnjaAwTHQr+WhtRTjQ" +
                        "\n" +
                        "淡淡的五彩光芒笼罩下，生机并未消逝。反而因目睹这份神秘活力而沉沦的人只增不减。" +
                        "\n" +
                        "'新犄角开始缓缓生长了呢…'";
        System.out.println(insertObfuscatedText(sample,"[内容来自拼好书]"));   // → 2478
    }

    /**
     * 将Long数字的每一位嵌入到多行文本中（每行嵌入一位数字）
     * @param number 要嵌入的数字
     * @param multiLineText 多行文本
     * @return 嵌入数字后的文本
     */
    public static String embedDigitsInText(Long number, String multiLineText) {
        if (number == null) {
            return multiLineText;
        }

        // 将数字转换为字符串并分割为单个字符
        String numStr = number.toString();
        char[] digits = numStr.toCharArray();

        // 分割文本为行
        String[] lines = multiLineText.split("\n");
        List<String> resultLines = new ArrayList<>();

        // 为每行嵌入数字（如果数字位数多于行数，多余数字将被忽略）
        int minLines = Math.min(digits.length, lines.length);

        for (int i = 0; i < minLines; i++) {
            char digit = digits[i];
            if (Character.isDigit(digit)) {
                int digitValue = digit - '0';
                // 获取该数字对应的零宽序列
                String zeroWidthSeq = DIGIT_TO_ZERO_WIDTH[digitValue];
                // 添加到行尾
                resultLines.add(lines[i] + zeroWidthSeq);
            } else {
                // 非数字字符直接添加
                resultLines.add(lines[i]);
            }
        }

        // 处理剩余行（如果有）
        for (int i = minLines; i < lines.length; i++) {
            resultLines.add(lines[i]);
        }

        // 重新组合为多行文本
        return String.join("\n", resultLines);
    }

    /**
     * 混淆文本（添加零宽字符和随机换行）
     */
    public static String obfuscateText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder sb = new StringBuilder();
        char[] chars = text.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            sb.append(chars[i]);

            if (i < chars.length - 1) {
                // 随机插入1-3个零宽字符
                int count = 1 + random.nextInt(3);
//                for (int j = 0; j < count; j++) {
//                    char zc = ZERO_WIDTH_CHARS[random.nextInt(ZERO_WIDTH_CHARS.length)];
//                    sb.append(zc);
//                }

                // 20%概率插入换行符
                if (random.nextDouble() < 0.2) {
                    sb.append('\n');
                }
            }
        }

        return sb.toString();
    }

    /**
     * 将混淆文本随机插入到多行文本中
     */
    public static String insertObfuscatedText(String originalContent, String fixedText) {
        // 混淆固定文本
        String obfuscatedText = obfuscateText(fixedText);

        // 分割原始文本为多行
        String[] lines = originalContent.split("\n");
        List<String> lineList = new ArrayList<>(List.of(lines));

        // 随机选择插入位置（0 到 行数之间）
        int insertPosition = random.nextInt(lineList.size() + 1);

        // 插入混淆文本
        lineList.add(insertPosition, obfuscatedText);

        // 重新组合为多行文本
        return String.join("\n", lineList);
    }
}