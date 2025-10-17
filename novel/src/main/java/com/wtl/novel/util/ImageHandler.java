package com.wtl.novel.util;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class ImageHandler {

    /**
     * 判断是否为Base64图片
     */
    public static boolean isBase64Image(String imageUrl) {
        return imageUrl.startsWith("data:image/");
    }

    /**
     * 处理Base64图片
     */
    public static String handleBase64Image(String base64ImageUrl, String saveDir) throws Exception {
        String[] parts = base64ImageUrl.split(",");
        String mimeType = parts[0].split(";")[0].split(":")[1];
        String base64Data = parts[1];

        String fileExtension = "";
        if (mimeType.equals("image/jpeg")) {
            fileExtension = ".jpg";
        } else if (mimeType.equals("image/png")) {
            fileExtension = ".png";
        } else if (mimeType.equals("image/gif")) {
            fileExtension = ".gif";
        } else {
            throw new Exception("Unsupported image type: " + mimeType);
        }
        String fileName = UUID.randomUUID().toString() + fileExtension;
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        return saveImageToLocal(imageBytes, saveDir, fileName);
    }

    /**
     * 处理普通URL图片
     */
    public static String handleUrlImage(String imageUrl, String saveDir) throws Exception {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            String fileName = getString(imageUrl, connection);
            return saveImageToLocal(connection.getInputStream(), saveDir, fileName);
        } else {
            throw new Exception("Failed to retrieve image from URL: " + connection.getResponseCode());
        }
    }

    @NotNull
    private static String getString(String imageUrl, HttpURLConnection connection) throws Exception {
        String fileName = extractFileName(imageUrl);
        if (!fileName.contains(".")) {
            String contentType = connection.getContentType();
            if (contentType.contains("image/jpeg")) {
                fileName += ".jpg";
            } else if (contentType.contains("image/png")) {
                fileName += ".png";
            } else if (contentType.contains("image/gif")) {
                fileName += ".gif";
            } else {
                throw new Exception("Unsupported image type: " + contentType);
            }
        }
        return fileName;
    }
    public static String extractFileName(String imageUrl) {
        // 提取 URL 的最后一部分作为文件名
        String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);

        // 如果文件名包含查询参数，移除查询参数
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf('?'));
        }

        // 如果文件名包含锚点，移除锚点
        if (fileName.contains("#")) {
            fileName = fileName.substring(0, fileName.indexOf('#'));
        }

        // 如果文件名没有扩展名，尝试根据常见的图像扩展名进行适配
        if (!fileName.contains(".")) {
            // 定义常见的图像文件扩展名
            String[] imageExtensions = { "jpg", "jpeg", "png", "gif", "webp", "svg", "bmp" };

            // 尝试判断文件名是否以常见的扩展名之一结尾
            boolean hasExtension = false;
            for (String ext : imageExtensions) {
                if (fileName.toLowerCase().endsWith(ext)) {
                    hasExtension = true;
                    break;
                }
            }

            // 如果没有匹配的扩展名，添加默认扩展名 .jpg
            if (!hasExtension) {
                fileName += ".jpg";
            }
        }

        return fileName;
    }
    /**
     * 保存图片到本地
     */
    private static String saveImageToLocal(byte[] imageBytes, String saveDir, String fileName) throws IOException {
        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(imageBytes);
        fos.close();
        return file.getAbsolutePath();
    }

    /**
     * 保存图片到本地
     */
    private static String saveImageToLocal(InputStream inputStream, String saveDir, String fileName) throws IOException {
        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }
        fos.close();
        inputStream.close();
        return file.getAbsolutePath();
    }
}