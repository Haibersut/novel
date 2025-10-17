package com.wtl.novel.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtl.novel.Service.ChapterService;
import com.wtl.novel.entity.Dictionary;
import com.wtl.novel.repository.DictionaryRepository;
import jakarta.annotation.PostConstruct;
import org.apache.fontbox.ttf.CmapTable;
import org.apache.fontbox.ttf.CmapSubtable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ObfuscateFontOTF {

    public Map<String, String> fontMap = new HashMap<>();
    public Map<String, String> reversedFontMap = new HashMap<>();

    @Autowired
    private DictionaryRepository dictionaryRepository;
    @Autowired
    private ChapterService chapterService;
    @Autowired
    private CloudflareR2Util cloudflareR2Util;
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void initFontMap() {
        try {
            String targetDirectoryBasedOnOS = cloudflareR2Util.getTempDirectory();
            String mappingFile = targetDirectoryBasedOnOS + "mapping.json";
            File file = new File(mappingFile);
            if (file.exists()) {
                fontMap = mapper.readValue(
                        file,
                        new TypeReference<Map<String, String>>() {}
                );
                reversedFontMap = fontMap.entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getValue,
                                Map.Entry::getKey,
                                (v1, v2) -> v1   // 如有重复值，保留先出现的
                        ));
                System.out.println("字体映射表已加载，共 " + fontMap.size() + " 条映射");
            } else {
                System.out.println("mapping.json 文件不存在，fontMap 为空");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("初始化加载字体映射失败");
        }
    }

    public static Map<Character, Character> obfuscateFont(
            String inputFontPath,
            String outputFontPath,
            Set<Character> textChars,
            int startUnicode,
            int endUnicode
    ) throws IOException {
        // 解析字体
        TTFParser parser = new TTFParser();
        TrueTypeFont font = parser.parse(new File(inputFontPath));

        // 获取 cmap
        CmapTable cmapTable = font.getCmap();
        CmapSubtable[] cmaps = cmapTable.getCmaps();
        CmapSubtable unicodeCmap = null;
        for (CmapSubtable cmap : cmaps) {
            if (cmap.getPlatformId() == 3 && (cmap.getPlatformEncodingId() == 1 || cmap.getPlatformEncodingId() == 10)) {
                unicodeCmap = cmap;
                break;
            }
        }
        if (unicodeCmap == null) {
            throw new RuntimeException("没有找到 Unicode cmap");
        }

        // 已有的 unicode 编码
        Set<Integer> existingUnicodes = new HashSet<>();
        for (char ch : textChars) {
            int gid = unicodeCmap.getGlyphId(ch);
            if (gid != 0) {
                existingUnicodes.add((int) ch);
            }
        }

        // 生成可用的 PUA 范围
        List<Integer> available = new ArrayList<>();
        for (int cp = startUnicode; cp <= endUnicode; cp++) {
            if (!existingUnicodes.contains(cp)) {
                available.add(cp);
            }
        }

        if (available.size() < textChars.size()) {
            throw new RuntimeException("可用 Unicode 不足以映射所有字符，请扩大范围");
        }

        // 打乱可用范围
        Collections.shuffle(available);

        Map<Character, Character> mapping = new LinkedHashMap<>();
        int idx = 0;

        for (Character ch : textChars) {
            int gid = unicodeCmap.getGlyphId(ch);
            if (gid == 0) {
                System.out.println("警告：字体中没有找到字符 " + ch);
                continue;
            }
            int fakeCode = available.get(idx++);
            mapping.put(ch, (char) fakeCode);
        }

        // ⚠ 注意：
        // FontBox/PDFBox 没有直接提供写回字体 cmap 的 API
        // 如果你要生成新的 OTF/TTF 文件，需要额外处理（比如用 sfntly 或调用 fonttools 命令行）。
        // 这里仅演示映射关系生成。
        font.close();

        return mapping;
    }

//    @Scheduled(fixedDelay = 3 * 24 * 60 * 60 * 1000)
    public void process() {
        String txtFile = chapterService.getRandomChapter();
        String targetDirectoryBasedOnOS = cloudflareR2Util.getTempDirectory();
        String inputFont = targetDirectoryBasedOnOS + "bb.otf";
        String outputFont = targetDirectoryBasedOnOS+"novelFont.otf"; // ⚠ 当前版本不会生成，仅做占位
        String mappingFile = targetDirectoryBasedOnOS+"mapping.json";
        Dictionary fontVersion = dictionaryRepository.getDictionaryByKeyField("fontVersion");
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb = new ProcessBuilder(
                    osName.contains("win")?"py":"python3", targetDirectoryBasedOnOS+"test1.py", inputFont, outputFont,mappingFile, txtFile
            );

            // 设置工作目录（如果脚本和字体在项目根目录）

            // 合并错误输出和标准输出
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // 读取 Python 输出
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[Python] " + line);
                }
            }

            int exitCode = process.waitFor();
            System.out.println("Python 脚本执行完毕，退出码: " + exitCode);
            if (exitCode == 0) {
                ObjectMapper mapper = new ObjectMapper();
                fontVersion.setValueField(String.valueOf(Integer.parseInt(fontVersion.getValueField()) + 1));
                cloudflareR2Util.uploadImageToCloudflareR2(new File(outputFont), "novelFont" + fontVersion.getValueField() + ".otf");
                dictionaryRepository.save(fontVersion);
                fontMap =  mapper.readValue(
                        new File(mappingFile),
                        new TypeReference<Map<String, String>>() {}
                );
                reversedFontMap = fontMap.entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getValue,
                                Map.Entry::getKey,
                                (v1, v2) -> v1   // 如有重复值，保留先出现的
                        ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        String targetDirectoryBasedOnOS = System.getProperty("file.upload.temp-dir", 
            System.getenv().getOrDefault("FILE_UPLOAD_TEMP_DIR", "/home/novel/tmp/"));
        String inputFont = targetDirectoryBasedOnOS + "bb.otf";
        String outputFont = targetDirectoryBasedOnOS+"novelFont.otf"; // ⚠ 当前版本不会生成，仅做占位
        String mappingFile = targetDirectoryBasedOnOS+"mapping.json";
        String txtFile = "我说好人啊aa";

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "py", "C:\\Users\\30402\\Desktop\\NTR\\333\\test1.py", inputFont, outputFont,mappingFile, txtFile
            );

            // 设置工作目录（如果脚本和字体在项目根目录）

            // 合并错误输出和标准输出
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // 读取 Python 输出
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[Python] " + line);
                }
            }

            int exitCode = process.waitFor();
            System.out.println("Python 脚本执行完毕，退出码: " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
