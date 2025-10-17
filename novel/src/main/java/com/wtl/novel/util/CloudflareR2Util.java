package com.wtl.novel.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

@Component
public class CloudflareR2Util {

    private static final Logger logger = LoggerFactory.getLogger(CloudflareR2Util.class);

    // Cloudflare R2 配置
    @Value("${cloudflare.r2.account-id}")
    private String accountId;
    
    @Value("${cloudflare.r2.access-key}")
    private String accessKey;
    
    @Value("${cloudflare.r2.secret-key}")
    private String secretKey;
    
    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;
    
    @Value("${cloudflare.r2.region:auto}")
    private String region;

    @Value("${cloudflare.r2.enabled:true}")
    private boolean r2Enabled;

    @Value("${file.upload.temp-dir:/home/novel/tmp/}")
    private String tempDirectory;

    @Value("${file.upload.storage-dir:/home/novel/file/}")
    private String storageDirectory;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        if (!r2Enabled) {
            logger.info("Cloudflare R2 已禁用,跳过初始化");
            return;
        }
        
        if (accountId == null || accountId.isEmpty() || 
            accessKey == null || accessKey.isEmpty() || 
            secretKey == null || secretKey.isEmpty()) {
            logger.warn("Cloudflare R2 配置不完整,跳过初始化");
            return;
        }
        
        try {
            String endpoint = String.format("https://%s.r2.cloudflarestorage.com", accountId);
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            this.s3Client = S3Client.builder()
                    .endpointOverride(URI.create(endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.of(region))
                    .build();
            logger.info("Cloudflare R2 客户端初始化成功");
        } catch (Exception e) {
            logger.error("Cloudflare R2 客户端初始化失败: {}", e.getMessage());
            logger.warn("R2 客户端初始化失败,但应用将继续运行");
        }
    }

    @PreDestroy
    public void destroy() {
        if (s3Client != null) {
            try {
                s3Client.close();
                logger.info("Cloudflare R2 客户端已关闭");
            } catch (Exception e) {
                logger.error("关闭 Cloudflare R2 客户端时出错", e);
            }
        }
    }

    
    // 下载图片到本地
    public String downloadImageToLocalStorage(String imageUrl, String localFilePath) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(30000);
            connection.setConnectTimeout(30000);
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                logger.error("下载图片失败，HTTP 状态码：{}", connection.getResponseCode());
                return null;
            }
            Path path = Paths.get(localFilePath);
            Files.createDirectories(path.getParent());
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream fileOutputStream = new FileOutputStream(localFilePath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }
            return localFilePath;
        } catch (Exception e) {
            logger.error("下载图片到本地失败：{}，错误：{}", imageUrl, e.getMessage(), e);
            return null;
        }
    }


    // 上传图片到 Cloudflare R2
    public String uploadImageToCloudflareR2(String localFilePath, String objectKey) {
        if (s3Client == null) {
            logger.warn("Cloudflare R2 客户端未初始化,无法上传图片: {}", objectKey);
            return null;
        }
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(getContentType(localFilePath))
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromFile(new File(localFilePath)));
            String r2Url = String.format("https://%s.%s.r2.cloudflarestorage.com/%s", bucketName, accountId, objectKey);
            logger.info("上传图片到 R2 成功：{}", objectKey);
            return r2Url;
        } catch (Exception e) {
            logger.error("上传图片到 Cloudflare R2 失败：{}，错误：{}", objectKey, e.getMessage(), e);
            return null;
        }
    }

    // 上传图片到 Cloudflare R2
    public String uploadImageToCloudflareR2(File localFile, String objectKey) {
        if (s3Client == null) {
            logger.warn("Cloudflare R2 客户端未初始化,无法上传图片: {}", objectKey);
            return null;
        }
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(getContentType(localFile.getCanonicalPath()))
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromFile(localFile));
            String r2Url = String.format("https://%s.%s.r2.cloudflarestorage.com/%s", bucketName, accountId, objectKey);
            logger.info("上传图片到 R2 成功：{}", objectKey);
            return r2Url;
        } catch (Exception e) {
            logger.error("上传图片到 Cloudflare R2 失败：{}，错误：{}", objectKey, e.getMessage(), e);
            return null;
        }
    }

    // 根据文件扩展名获取 Content-Type
    private static String getContentType(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "jpg", "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "txt":
                return "text/plain";
            case "html":
                return "text/html";
            case "css":
                return "text/css";
            case "js":
                return "application/javascript";
            default:
                return "application/octet-stream";
        }
    }

    // 替换图片链接
    public String replaceImageUrl(String contentLink, String oldSrc, String newSrc) {
        return contentLink.replaceAll("src\\s*=\\s*\"" + Pattern.quote(oldSrc) + "\"", "src=\"" + newSrc + "\"");
    }

    public String getTempDirectory() {
        File directory = new File(tempDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return tempDirectory;
    }

    public String getStorageDirectory() {
        File directory = new File(storageDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return storageDirectory;
    }

    // 使用 Files.copy 方法保存文件到本地
    public String saveToFileSystem(MultipartFile multipartFile, String targetDirectory) throws IOException {
        String filePath = targetDirectory + File.separator + multipartFile.getOriginalFilename();
        File directory = new File(targetDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        Path targetPath = Path.of(filePath);
        Files.copy(multipartFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        logger.info("文件保存成功：{}", filePath);
        return filePath;
    }
}