package com.wtl.novel.util;

public class MaskStringUtil {

    /**
     * 屏蔽字符串中间部分，用 * 号代替
     *
     * @param str 原始字符串
     * @return 屏蔽后的字符串
     */
    public static String maskMiddle(String str) {
        if (str == null || str.isEmpty()) {
            return str; // 如果字符串为空或 null，直接返回
        }

        int length = str.length();
        int start = 1; // 保留的开头字符数
        int end = 1;   // 保留的结尾字符数

        // 如果字符串长度不足以保留 start 和 end，直接用 * 代替
        if (length <= start + end) {
            return "*".repeat(length); // 用 * 填充整个字符串
        }

        // 保留开头部分
        int middleLength = length - start - end;
        // 屏蔽中间部分，用 * 号代替
        // 保留结尾部分

        return str.substring(0, start) +
                // 屏蔽中间部分，用 * 号代替
                "*" +
                // 保留结尾部分
                str.substring(length - end);
    }

}
