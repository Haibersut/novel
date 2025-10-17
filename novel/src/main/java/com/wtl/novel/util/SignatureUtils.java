package com.wtl.novel.util;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignatureUtils {

    private static final Logger log = LoggerFactory.getLogger(SignatureUtils.class);

    /**
     * 验证签名是否有效
     *
     * @param requestSignature 请求头中的签名
     * @param timestamp        请求头中的时间戳
     * @param nonce            请求头中的随机数
     * @param method           请求方法
     * @param path             请求路径
     * @param params           请求参数
     * @param data             请求体数据
     * @param secretKey        密钥
     * @return 签名是否有效
     */
    public static boolean validateSignature(
            String requestSignature,
            String timestamp,
            String nonce,
            String method,
            String path,
            Map<String, String> params,
            String data,
            String secretKey) {

        // 构造签名字符串
        String signStr = buildSignString(method, path, params, data, timestamp, nonce);

        // 使用密钥生成签名
        String generatedSignature = hmacSHA256(signStr, secretKey);
        // 比较签名
        return generatedSignature.equalsIgnoreCase(requestSignature);
    }

    /**
     * 构造签名字符串
     *
     * @param method   请求方法
     * @param path     请求路径
     * @param params   请求参数
     * @param data     请求体数据
     * @param timestamp 时间戳
     * @param nonce    随机数
     * @return 签名字符串
     */
    private static String buildSignString(
            String method,
            String path,
            Map<String, String> params,
            String data,
            String timestamp,
            String nonce) {

        // 对参数按 key 排序
        Map<String, String> sortedParams = new TreeMap<>(params);

        // 将参数和数据转换为 JSON 字符串
        String paramsStr;
        String dataStr = data;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            paramsStr = objectMapper.writeValueAsString(sortedParams);
            if (data == null || data.isEmpty()) {
                dataStr = "{}"; // 当 data 为空时，设置为 "{}"
            } else {
                // 对 data 中的 key 进行排序
                dataStr = sortAndSerializeData(objectMapper, data);
            }
        } catch (Exception e) {
            log.error("Failed to serialize params or data", e);
            throw new RuntimeException("Failed to serialize params or data", e);
        }

        // 构造签名字符串
        return String.format("%s|%s|%s|%s|%s|%s",
                method,
                path,
                paramsStr,
                dataStr,
                timestamp,
                nonce);
    }

    /**
     * 对 data 中的 key 进行排序并序列化为 JSON 字符串
     *
     * @param objectMapper Jackson 的对象映射器
     * @param data         请求体数据
     * @return 排序并序列化后的 JSON 字符串
     */
    private static String sortAndSerializeData(ObjectMapper objectMapper, String data) {
        try {
            // 将 JSON 字符串解析为 Map
            Map<String, Object> map = objectMapper.readValue(data, Map.class);

            // 对 Map 的 key 进行排序
            Map<String, Object> sortedMap = new TreeMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                sortedMap.put(entry.getKey(), sortObject(entry.getValue()));
            }

            // 序列化为 JSON 字符串
            return objectMapper.writeValueAsString(sortedMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sort and serialize data", e);
        }
    }

    /**
     * 递归对对象进行排序（用于处理嵌套对象）
     *
     * @param obj 需要排序的对象
     * @return 排序后的对象
     */
    private static Object sortObject(Object obj) {
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            Map<String, Object> sortedMap = new TreeMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                sortedMap.put(entry.getKey(), sortObject(entry.getValue()));
            }
            return sortedMap;
        } else if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            return list.stream().map(SignatureUtils::sortObject).collect(Collectors.toList());
        } else {
            return obj;
        }
    }

    /**
     * 使用 HmacSHA256 生成签名
     *
     * @param data     需要签名的数据
     * @param key      密钥
     * @return 签名字符串
     */
    private static String hmacSHA256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC-SHA256 signature", e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16;

    public static String encrypt(String plaintext, String keyStr) throws Exception {
        long currentMinute = System.currentTimeMillis() / 60000 * 60000;
        String combinedKey = keyStr + currentMinute;

        // 生成密钥
        SecretKeySpec secretKey = generateSecretKey(combinedKey);
        // 生成随机IV
        byte[] ivBytes = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(ivBytes);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] combined = new byte[ivBytes.length + encrypted.length];
        System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
        System.arraycopy(encrypted, 0, combined, ivBytes.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String ciphertext, String keyStr) throws Exception {
        byte[] combined = Base64.getDecoder().decode(ciphertext);
        byte[] ivBytes = Arrays.copyOfRange(combined, 0, IV_SIZE);
        byte[] encrypted = Arrays.copyOfRange(combined, IV_SIZE, combined.length);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        // 尝试当前分钟和前1分钟的时间戳
        List<Long> timestamps = getPossibleTimestamps();
        for (Long timestamp : timestamps) {
            try {
                String combinedKey = keyStr + timestamp;
                SecretKeySpec secretKey = generateSecretKey(combinedKey);

                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
                byte[] decrypted = cipher.doFinal(encrypted);

                return new String(decrypted, StandardCharsets.UTF_8);
            } catch (Exception ignored) {
                // 解密失败尝试下一个时间戳
            }
        }
        throw new IllegalArgumentException("解密失败");
    }

    private static SecretKeySpec generateSecretKey(String combinedKey) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(combinedKey.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "AES");
    }

    private static List<Long> getPossibleTimestamps() {
        long current = System.currentTimeMillis() / 60000 * 60000;
        return Arrays.asList(current, current - 60000);
    }
}