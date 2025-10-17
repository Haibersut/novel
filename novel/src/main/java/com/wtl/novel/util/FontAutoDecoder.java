package com.wtl.novel.util;

import org.apache.fontbox.ttf.CmapSubtable;
import org.apache.fontbox.ttf.CmapTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

/**
 * FontAutoDecoder
 *
 * 1) generateMapping: 根据反爬字体 + 正常字体 + 候选汉字集 自动生成 mapping.csv
 * 2) decodeText: 使用 mapping.csv 把 PUA 文本还原为汉字
 *
 * 使用:
 *  - 准备:
 *      dc027189e0ba4cd.woff  (@反爬字体)
 *      candidates.txt        (@候选汉字，放常用字)
 *  - 生成映射:
 *      java FontAutoDecoder generateMapping dc027189e0ba4cd.woff "SimSun" candidates.txt mapping.csv
 *  - 还原文本:
 *      java FontAutoDecoder decodeText mapping.csv "梦析"
 *
 * 注意: 若系统没有 SimSun，可改为 "Noto Sans CJK SC" / "Microsoft YaHei" / 其他可用中文字体名。
 */
public class FontAutoDecoder {


    private static final Map<Integer, Integer> FAKE_TO_REAL_MAP = new HashMap<>();

    // 初始化时加载字体映射关系
    static {
        try {
            // 1. 字体文件路径配置
            String storageDir = System.getProperty("file.upload.storage-dir", 
                System.getenv().getOrDefault("FILE_UPLOAD_STORAGE_DIR", "/home/novel/file/"));
            String standardFontPath = storageDir + "NotoSansKR-VariableFont_wght.ttf";
            String obfuscatedFontPath = "C:\\Users\\dc027189e0ba4cd.woff";

            // 2. 解析标准字体和混淆字体
            TTFParser parser = new TTFParser();
            TrueTypeFont standardFont = parser.parse(new File(standardFontPath));
            TrueTypeFont obfuscatedFont = parser.parse(new File(obfuscatedFontPath));

            // 3. 构建映射表
            Map<Integer, Integer> standardGlyphToCode = parseCmap(standardFont.getCmap());
            Map<Integer, Integer> obfuscatedGlyphToCode = parseCmap(obfuscatedFont.getCmap());

            // 4. 建立混淆Unicode到真实Unicode的映射
            for (Map.Entry<Integer, Integer> entry : obfuscatedGlyphToCode.entrySet()) {
                int glyphId = entry.getKey();
                int fakeCode = entry.getValue();
                if (standardGlyphToCode.containsKey(glyphId)) {
                    FAKE_TO_REAL_MAP.put(fakeCode, standardGlyphToCode.get(glyphId));
                }
            }

            // 5. 关闭字体资源
            standardFont.close();
            obfuscatedFont.close();

        } catch (IOException e) {
            throw new RuntimeException("字体文件加载失败", e);
        }
    }

    /**
     * 将混淆字符串转换为真实文字
     */
    public static String deobfuscate(String obfuscatedText) {
        StringBuilder realText = new StringBuilder();
        for (char c : obfuscatedText.toCharArray()) {
            int realCode = FAKE_TO_REAL_MAP.getOrDefault((int) c, (int) c);
            realText.append((char) realCode);
        }
        return realText.toString();
    }

    /**
     * 解析cmap表（兼容FontBox 2.0.28+）
     */
    private static Map<Integer, Integer> parseCmap(CmapTable cmap) {
        Map<Integer, Integer> glyphToCode = new HashMap<>();
        if (cmap == null) return glyphToCode;

        // 遍历所有子表，选择Windows Unicode子表（PlatformID=3, EncodingID=1）
        for (CmapSubtable subtable : cmap.getCmaps()) {
            if (subtable.getPlatformId() == 3 && subtable.getPlatformEncodingId() == 1) {
                // 遍历BMP范围字符（0x0000 - 0xFFFF）
                for (int codePoint = 0; codePoint <= 0xFFFF; codePoint++) {
                    int glyphId = subtable.getGlyphId(codePoint);
                    if (glyphId > 0) {
                        glyphToCode.put(glyphId, codePoint);
                    }
                }
                break;
            }
        }
        return glyphToCode;
    }
}
