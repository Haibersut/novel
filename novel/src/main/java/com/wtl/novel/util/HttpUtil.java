package com.wtl.novel.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HttpUtil {

    // URL 编码方法
    public static String encodeUrl(String url) {
        try {
            // 使用 URLEncoder 对查询参数进行编码
            String[] urlParts = url.split("\\?", 2);
            if (urlParts.length == 2) {
                String path = urlParts[0];
                String queryParams = urlParts[1];
                String encodedQueryParams = URLEncoder.encode(queryParams, StandardCharsets.UTF_8).replace("+", "%20");
                return path + "?" + encodedQueryParams;
            } else {
                return URLEncoder.encode(url, StandardCharsets.UTF_8).replace("+", "%20");
            }
        } catch (Exception e) {
            throw new RuntimeException("URL encoding failed", e);
        }
    }
}
