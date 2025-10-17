package com.wtl.novel.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageResizer {

    // 缩小比例
    private static final double SCALE = 1.0 / 3.0;


    private static void resizeImage(File file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file);
        if (originalImage == null) {
            throw new IOException("无法读取图像: " + file.getName());
        }

        int newWidth = (int) (originalImage.getWidth() * SCALE);
        int newHeight = (int) (originalImage.getHeight() * SCALE);

        Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        BufferedImage outputImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        // 覆盖原图（如需另存新文件，请修改这里的输出路径）
        ImageIO.write(outputImage, "png", file);
    }
}


//    public static void main(String[] args) {
//        // 示例：压缩指定文件夹下的所有图片
//        String folderPath = "C:\\Users\\30402\\Desktop\\新建文件夹 (6)\\output\\image\\我成为了异世界论坛的管理员2 - 副本";
//        compressAndOverwriteImages(folderPath, 500, 0.99f);
//    }