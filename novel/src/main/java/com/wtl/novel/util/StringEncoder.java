package com.wtl.novel.util;

import com.wtl.novel.entity.Chapter;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

public class StringEncoder {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY = "CannCCanneedtKey"; // 16/24/32字节密钥
    private static final int IV_SIZE = 16; // AES块大小

    /**
     * 清理文本中的加密行
     * @param content 待清理的文本
     * @return 清理后的文本
     */
    public static String cleanText(String content) {
        if (content == null || content.isEmpty()) return content;

        List<String> cleanedLines = new ArrayList<>();
        String[] lines = content.split("\n");

        for (String line : lines) {
            String trimmedLine = line.trim();
            // 跳过空行
            if (trimmedLine.isEmpty()) {
                cleanedLines.add(line);
                continue;
            }

            // 检测并跳过加密行
            if (!isEncodedText(trimmedLine)) {
                cleanedLines.add(line);
            }
        }
        return String.join("\n", cleanedLines);
    }

    /**
     * 检查字符串是否为有效加密文本
     */
    private static boolean isEncodedText(String text) {
        try {
            // 检查Base64格式
            if (!isValidBase64(text)) return false;

            // 尝试解密验证
            decode(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证Base64格式有效性
     */
    private static boolean isValidBase64(String text) {
        if (text.length() % 4 != 0) return false; // Base64长度需为4的倍数
        try {
            Base64.getDecoder().decode(text);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }


    // 编码（每次结果不同）
    public static String encode(String input) throws Exception {
        // 生成随机IV
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // 准备密钥
        SecretKeySpec keySpec = new SecretKeySpec(
                KEY.getBytes(StandardCharsets.UTF_8),
                "AES"
        );

        // 加密数据
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));

        // 组合IV+加密数据
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }


    // 在随机行插入编码内容
    public static String insertEncodedTextRandomly(String originalContent, String textToEncode)  {
        // 1. 获取原始内容

        // 2. 编码要插入的文本
        String encodedText = null;
        try {
            encodedText = StringEncoder.encode(textToEncode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 3. 按行分割内容
        String[] lines = originalContent.split("\n");
        List<String> lineList = new ArrayList<>(Arrays.asList(lines));

        // 4. 随机选择插入位置 (避开首行和末行)
        int insertPos = new Random().nextInt(Math.max(1, lineList.size() - 1)) + 1;

        // 5. 插入编码文本
        lineList.add(insertPos, encodedText);

        // 6. 合并并保存回chapter
        return String.join("\n", lineList);
    }

    // 解码（还原原始内容）
    public static String decode(String encoded) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encoded);

        // 分离IV和加密数据
        byte[] iv = new byte[IV_SIZE];
        byte[] encrypted = new byte[combined.length - IV_SIZE];
        System.arraycopy(combined, 0, iv, 0, IV_SIZE);
        System.arraycopy(combined, IV_SIZE, encrypted, 0, encrypted.length);

        // 准备密钥和IV
        SecretKeySpec keySpec = new SecretKeySpec(
                KEY.getBytes(StandardCharsets.UTF_8),
                "AES"
        );
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // 解密数据
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        byte[] decrypted = cipher.doFinal(encrypted);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

}