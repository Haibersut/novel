package com.wtl.novel.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import java.util.Iterator;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import java.util.Iterator;


public class TextImageGenerator {

    /**
     * 根据文本内容生成图片，并以 Base64 编码字符串形式返回。
     *
     * @param hexBgColor        背景色，如"#FFFFFF"。
     * @param fontSize          字体大小（像素）。
     * @param lineSpacing       行间距（像素）。
     * @param hexTextColor      字体颜色，如"#000000"。
     * @param imageWidth        图片宽度（像素）。
     * @param textContent       要渲染的文本内容。
     * @param format            输出图片格式，如 "png" 或 "jpeg"。
     * @param compressionQuality 对于 JPEG 格式，介于 0.0（最低质量）和 1.0（最高质量）之间。对 PNG 无效。
     * @return 包含数据 URI 前缀的图片 Base64 编码字符串。
     * @throws Exception 如果在图片生成或编码过程中发生错误。
     */
    public static String generateTextImage(
            String hexBgColor,      // 背景色，如"#FFFFFF"
            int fontSize,           // 字体大小（像素）
            int lineSpacing,        // 行间距（像素）
            String hexTextColor,    // 字体颜色，如"#000000"
            int imageWidth,         // 图片宽度（像素）
            String textContent,     // 文本内容
            String format,          // 图片格式，如 "png", "jpeg"
            float compressionQuality // 压缩质量，仅对 JPEG 有效 (0.0 - 1.0)
    ) throws Exception {

        // 1. 颜色转换
        Color bgColor = hexToColor(hexBgColor);
        Color textColor = hexToColor(hexTextColor);

        // 2. 创建临时Graphics对象计算文本尺寸
        BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tempG = tempImg.createGraphics();
        // 使用支持中文的字体，确保系统中有此字体，否则可能使用默认字体
        Font font = new Font("Microsoft YaHei", Font.PLAIN, fontSize);
        tempG.setFont(font);
        FontMetrics fm = tempG.getFontMetrics();

        // 3. 文本换行处理
        // 左右各留10px边距，所以可用宽度为 imageWidth - 20
        List<String> lines = wrapText(textContent, imageWidth - 20, fm);

        // 4. 计算图片高度
        int lineHeight = fm.getHeight();
        int imageHeight = lineHeight * lines.size() +
                lineSpacing * (lines.size() - 1) +
                20; // 上下边距 (10px top, 10px bottom)

        // 5. 创建实际图片
        // 对于 JPEG，通常偏好 TYPE_INT_RGB，因为它不支持透明度且文件可能更小。
        // 对于 PNG，TYPE_INT_ARGB 是合适的。
        BufferedImage image;
        if (format.equalsIgnoreCase("jpeg")) {
            image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        } else {
            image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D g2d = image.createGraphics();

        // 6. 绘制背景
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, imageWidth, imageHeight);

        // 7. 设置文字属性
        g2d.setFont(font);
        g2d.setColor(textColor);
        // 开启文本抗锯齿，使文字更平滑
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // 开启高质量渲染，对于非文本元素可能有用，文本主要靠KEY_TEXT_ANTIALIASING
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);


        // 8. 绘制文本（支持自动换行）
        int y = fm.getAscent() + 10; // 顶部边距
        for (String line : lines) {
            g2d.drawString(line, 10, y); // 左侧边距10px
            y += lineHeight + lineSpacing;
        }

        // 9. 释放资源并转换为Base64
        g2d.dispose();
        tempG.dispose();
        return imageToBase64(image, format, compressionQuality);
    }

    /**
     * 将十六进制颜色字符串（如 "#RRGGBB"）转换为 Color 对象。
     * @param hex 十六进制颜色字符串。
     * @return Color 对象。
     */
    private static Color hexToColor(String hex) {
        if (hex == null || !hex.matches("^#[0-9a-fA-F]{6}$")) {
            throw new IllegalArgumentException("Invalid hex color format. Expected #RRGGBB.");
        }
        return new Color(
                Integer.parseInt(hex.substring(1, 3), 16),
                Integer.parseInt(hex.substring(3, 5), 16),
                Integer.parseInt(hex.substring(5, 7), 16)
        );
    }

    /**
     * 根据最大宽度将文本内容包装成行列表。
     * 处理换行符作为段落分隔。
     * @param text 输入文本。
     * @param maxWidth 每行的最大宽度（像素）。
     * @param fm 用于测量文本宽度的 FontMetrics。
     * @return 字符串列表，每个字符串代表一行文本。
     */
    private static List<String> wrapText(String text, int maxWidth, FontMetrics fm) {
        List<String> lines = new ArrayList<>();

        // 优雅地处理 null 或空文本
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        // 按换行符分割文本以处理段落
        String[] paragraphs = text.split("\n", -1); // -1 以保留末尾的空字符串

        for (String para : paragraphs) {
            if (para.isEmpty()) {
                lines.add(""); // 对于连续的换行符，添加一个空行
                continue;
            }

            StringBuilder currentLine = new StringBuilder();
            int currentWidth = 0;

            // 对于中文等语言，按字符分割比按单词分割更合适。
            for (char c : para.toCharArray()) {
                int charWidth = fm.charWidth(c);

                // 检查添加当前字符是否超出 maxWidth
                // currentWidth > 0 确保在换行前至少添加一个字符
                if (currentWidth + charWidth > maxWidth && currentWidth > 0) {
                    // 如果当前行不为空，则将其添加到 lines
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                    currentWidth = 0;
                }

                currentLine.append(c);
                currentWidth += charWidth;
            }

            // 将 currentLine 中剩余的内容作为新行添加
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }

        // 确保如果原始文本为空或只有换行符，至少有一行空行
        if (lines.isEmpty()) {
            lines.add("");
        }

        return lines;
    }

    /**
     * 将 BufferedImage 转换为 Base64 编码字符串。
     * 支持 "png" 和 "jpeg" 格式，其中 JPEG 支持压缩。
     * @param image 要转换的 BufferedImage。
     * @param format 输出格式（"png" 或 "jpeg"）。
     * @param compressionQuality JPEG 的压缩质量（0.0 - 1.0）。对 PNG 无效。
     * @return 带有数据 URI 前缀的 Base64 编码字符串。
     * @throws Exception 如果在写入过程中发生错误。
     */
    private static String imageToBase64(BufferedImage image, String format, float compressionQuality) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (format.equalsIgnoreCase("jpeg")) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = null;
            if (writers.hasNext()) {
                writer = writers.next();
            } else {
                throw new IllegalStateException("未找到 JPEG ImageWriter。请确保 JPEG 插件可用。");
            }

            ImageWriteParam param = writer.getDefaultWriteParam();
            // 设置压缩模式为显式，然后设置质量
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(compressionQuality);

            writer.setOutput(ImageIO.createImageOutputStream(baos));
            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();

        } else if (format.equalsIgnoreCase("png")) {
            ImageIO.write(image, "png", baos);
        } else {
            throw new IllegalArgumentException("不支持的图片格式: " + format + "。仅支持 'png' 和 'jpeg'。");
        }

        return "data:image/" + format.toLowerCase() + ";base64," +
                Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    // 使用示例
    public static void main(String[] args) {
        try {
            String longText =
                    "早叫你少拈花惹草。战士和贤者就算了，连立过贞洁誓言的祭司都搞？真当我没发现？＂\n";

            System.out.println("--- 生成 PNG 图片（无损） ---");
            String base64PngImage = generateTextImage(
                    "#F0F8FF",      // 淡蓝色背景 (Alice Blue)
                    20,             // 20px 字体大小
                    8,              // 8px 行间距
                    "#333333",      // 深灰色文字
                    30,            // 400px 宽度
                    longText,
                    "png",          // PNG 格式
                    1.0f            // 压缩质量 (对 PNG 无效)
            );
            // 打印 Base64 字符串的一部分，避免控制台混乱
            System.out.println(base64PngImage);
            System.out.println("PNG Base64 字符串长度: " + base64PngImage.length());
//
//
//            System.out.println("\n--- 生成 JPEG 图片 (高品质，压缩比低) ---");
//            String base64JpegHighQualityImage = generateTextImage(
//                    "#F0F8FF",      // 淡蓝色背景
//                    20,             // 20px 字体大小
//                    8,              // 8px 行间距
//                    "#333333",      // 深灰色文字
//                    400,            // 400px 宽度
//                    longText,
//                    "jpeg",         // JPEG 格式
//                    0.9f            // 高品质 (90%)
//            );
//            System.out.println("JPEG 高品质图片数据 URI (前100字符): " + base64JpegHighQualityImage.substring(0, Math.min(base64JpegHighQualityImage.length(), 100)) + "...");
//            System.out.println("JPEG 高品质 Base64 字符串长度: " + base64JpegHighQualityImage.length());
//
//            System.out.println("\n--- 生成 JPEG 图片 (中等品质，压缩比高) ---");
//            String base64JpegMediumQualityImage = generateTextImage(
//                    "#F0F8FF",      // 淡蓝色背景
//                    20,             // 20px 字体大小
//                    8,              // 8px 行间距
//                    "#333333",      // 深灰色文字
//                    400,            // 400px 宽度
//                    longText,
//                    "jpeg",         // JPEG 格式
//                    0.5f            // 中等品质 (50%)
//            );
//
//            System.out.println(base64JpegMediumQualityImage);
//

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}