package com.wtl.novel.util;

import java.util.*;


public class QuoteModifier {


    // 映射表：将数字字符映射到引号对
    private final Map<Character, String[]> quoteMapping;
    // 反向映射表：将引号对映射到数字字符
    private final Map<String, Character> reverseQuoteMapping;

    // 所有需要筛选的原始引号对，用于在解析时进行匹配
    private static final String[][] ALL_QUOTE_PAIRS = {
            {"‘", "’"}, {"“", "”"}, {"‹", "›"}, {"«", "»"}, {"〝", "〞"}, {"〞", "〝"},
            {"❛", "❜"}, {"❝", "❞"}, {"´", "´"}, {"＂", "＂"}, {"【", "】"}, {"[", "]"}, {"ʻ", "’"}, {"‛", "’"},
            {"\"", "\""}, {"'", "'"}
    };

    /**
     * 构造函数，初始化引号的映射表和反向映射表。
     */
    public QuoteModifier() {
        // 初始化数字到引号对的映射
        quoteMapping = new HashMap<>();
        quoteMapping.put('0', new String[]{"‘", "’"});
        quoteMapping.put('1', new String[]{"“", "”"});
        quoteMapping.put('2', new String[]{"ʻ", "’"});
        quoteMapping.put('3', new String[]{"‛", "’"});
        quoteMapping.put('4', new String[]{"〝", "〞"});
        quoteMapping.put('5', new String[]{"❛", "❜"});
        quoteMapping.put('6', new String[]{"❝", "❞"});
        quoteMapping.put('7', new String[]{"´", "´"});
        quoteMapping.put('8', new String[]{"【", "】"});
        quoteMapping.put('9', new String[]{"'", "'"});

        // 初始化引号对到数字的反向映射
        reverseQuoteMapping = new HashMap<>();
        for (Map.Entry<Character, String[]> entry : quoteMapping.entrySet()) {
            reverseQuoteMapping.put(entry.getValue()[0] + entry.getValue()[1], entry.getKey());
        }
    }

    /**
     * 解析方法：给定文本和Long数字，将文本中包含引号的行进行筛选，并用Long数字对应的引号进行替换。
     *
     * @param originalText 原始文本。
     * @param number       一个Long数字，其每一位数字对应一个特定的引号对。
     * @return 替换引号后的文本。
     */
    public String parse(String originalText, Long number) {
        for (String[] quotePair : ALL_QUOTE_PAIRS) {
            originalText = originalText.replace(quotePair[0], "\"");
            originalText = originalText.replace(quotePair[1], "\"");
        }
        String[] lines = originalText.split("\n");
        List<Integer> quotedLineIndices = new ArrayList<>();
        List<String> quotedContents = new ArrayList<>();
        // 第一步：筛选出包含引号的行，并提取引号内的内容
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            for (String[] quotePair : ALL_QUOTE_PAIRS) {
                int startIndex = line.indexOf(quotePair[0]);
                // 查找结束引号，从开始引号之后的位置开始
                int endIndex = line.indexOf(quotePair[1], startIndex + quotePair[0].length());
                if (startIndex != -1 && endIndex != -1) {
                    // 找到了引号，将行索引和引号内的内容保存
                    quotedLineIndices.add(i);
                    quotedContents.add(line.substring(startIndex + quotePair[0].length(), endIndex));
                    // 假设每行只替换一个引号对，找到后即跳出内层循环
                    break;
                }
            }
        }

        // 第二步：根据 Long 数字获取新的引号对列表
        List<String[]> newQuotePairs = new ArrayList<>();
        String numberStr = String.valueOf(number);
        for (char digit : numberStr.toCharArray()) {
            newQuotePairs.add(quoteMapping.get(digit));
        }

        // 检查引号行数和数字位数是否匹配
//        if (quotedLineIndices.size() != newQuotePairs.size()) {
//            System.err.println("警告: 引号行数与数字位数不匹配。将只替换较少的那一部分。");
//        }

        // 第三步：进行替换操作，按照找到的行索引和数字顺序进行替换
        int replacementCount = Math.min(quotedLineIndices.size(), newQuotePairs.size());
        for (int i = 0; i < replacementCount; i++) {
            int lineIndex = quotedLineIndices.get(i);
            String content = quotedContents.get(i);
            String[] newQuotePair = newQuotePairs.get(i);
            // 重新构建该行，用新的引号包裹原有内容
            lines[lineIndex] = newQuotePair[0] + content + newQuotePair[1];
        }

        // 重新拼接所有行，返回最终文本
        return String.join("\n", lines);
    }

    /**
     * 反解析方法：给定替换后的文本，从包含特定引号的行中提取出对应的数字，并拼接成一个Long数字。
     *
     * @param modifiedText 经过解析后的文本。
     * @return 提取出的Long数字。
     */
    public String unparse(String modifiedText) {
        StringBuilder sb = new StringBuilder();
        String[] lines = modifiedText.split("\n");

        // 遍历每一行，查找新的引号对
        for (String line : lines) {
            for (Map.Entry<String, Character> entry : reverseQuoteMapping.entrySet()) {
                String quotePair = entry.getKey();
                String startQuote = quotePair.substring(0, 1);
                // 处理双字符引号，如´´，或者单引号 ""
                String endQuote = quotePair.length() > 1 ? quotePair.substring(1) : quotePair;

                if (line.contains(startQuote) && line.contains(endQuote)) {
                    // 找到了新的引号，将对应的数字字符追加到StringBuilder
                    sb.append(entry.getValue());
                    // 假设每行只包含一个新引号对，找到后即跳出内层循环
                    break;
                }
            }
        }

        if (sb.isEmpty()) {
            return "0"; // 如果没有找到任何引号，返回0
        }

        return sb.toString();
    }


    public static ProcessedResult processChapter(String content) {
        if (content == null || content.isEmpty()) {
            return new ProcessedResult(new ArrayList<>(), new HashMap<>());
        }

        List<String> lines = Arrays.asList(content.split("\n"));
        List<Map<String, Object>> displayLines = new ArrayList<>();
        Map<Integer, List<String>> extractedContent = new HashMap<>();

        final int totalParts = 8;
        int partSize = lines.size() == 0 ? 1 : (int) Math.ceil((double) lines.size() / totalParts);

        List<Integer> partIndices = new ArrayList<>();
        for (int i = 0; i < totalParts; i++) {
            partIndices.add(i);
        }

        List<Integer> selectedPartIndices = new ArrayList<>();
        int numPartsToSelect = Math.min(3, totalParts);
        Random random = new Random();
        while (selectedPartIndices.size() < numPartsToSelect) {
            int randomIndex = random.nextInt(partIndices.size());
            int selectedIndex = partIndices.remove(randomIndex);
            selectedPartIndices.add(selectedIndex);
        }
        Collections.sort(selectedPartIndices);

        Set<Integer> addedGetMarkers = new HashSet<>();

        for (int i = 0; i < lines.size(); i++) {
            String currentLine = lines.get(i);
            int currentPartIndex = i / partSize;
            boolean isSelectedForCanvas = selectedPartIndices.contains(currentPartIndex);
            boolean containsImgTag = currentLine.contains("<img");

            if (isSelectedForCanvas && !containsImgTag) {
                if (!addedGetMarkers.contains(currentPartIndex)) {
                    Map<String, Object> getMarker = new HashMap<>();
                    getMarker.put("type", "get");
                    getMarker.put("content", "get");
                    getMarker.put("partIndex", currentPartIndex);
                    displayLines.add(getMarker);
                    addedGetMarkers.add(currentPartIndex);
                }
                extractedContent.computeIfAbsent(currentPartIndex, k -> new ArrayList<>()).add(currentLine);
            } else {
                Map<String, Object> lineObject = new HashMap<>();
                lineObject.put("type", "line");
                lineObject.put("content", currentLine);
                lineObject.put("originalIndex", i);
                displayLines.add(lineObject);
            }

            if ((i + 1) % 50 == 0) {
                Map<String, Object> separator = new HashMap<>();
                separator.put("type", "separator");
                separator.put("content", "哈哈");
                displayLines.add(separator);
            }
        }

        return new ProcessedResult(displayLines, extractedContent);
    }

    /**
     * 章节数据的简单容器类。
     */
    public static class Chapter {
        private String content;

        public Chapter(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

    /**
     * 处理结果的容器类。
     */
    public static class ProcessedResult {
        private final List<Map<String, Object>> displayLines;
        private final Map<Integer, List<String>> extractedContent;

        public ProcessedResult(List<Map<String, Object>> displayLines, Map<Integer, List<String>> extractedContent) {
            this.displayLines = displayLines;
            this.extractedContent = extractedContent;
        }

        public List<Map<String, Object>> getDisplayLines() {
            return displayLines;
        }

        public Map<Integer, List<String>> getExtractedContent() {
            return extractedContent;
        }
    }

    // 测试用main方法
    public static void main(String[] args) {
        QuoteModifier modifier = new QuoteModifier();

        // 示例文本，包含3行需要替换的引号
        String exampleText = "目录\n" +
                "×\n" +
                "搜索章节\n" +
                "设置\n" +
                "×\n" +
                "阅读背景\n" +
                "\n" +
                "字体颜色\n" +
                "\n" +
                "字体大小\n" +
                "A-\n" +
                "24\n" +
                "A+\n" +
                "段距大小\n" +
                "A-\n" +
                "50\n" +
                "A+\n" +
                "阅读方式\n" +
                "手动换页\n" +
                "点击换页\n" +
                "\n" +
                "\uD83C\uDD98\n" +
                "帮助\n" +
                "\n" +
                "\uD83D\uDCD4\n" +
                "笔记\n" +
                "\n" +
                "⚙\uFE0F\n" +
                "设置\n" +
                "\n" +
                "\uD83C\uDF1E\n" +
                "白天模式\n" +
                "\n" +
                "\uD83D\uDCDD\n" +
                "目录\n" +
                "\n" +
                "\uD83D\uDC4E\uD83C\uDFFD\n" +
                "翻译问题\n" +
                "\n" +
                "\uD83D\uDD19\n" +
                "返回\n" +
                "添加笔记\n" +
                "EP0003\n" +
                "本章字数：4178\n" +
                "更新时间：2025-4-22\n" +
                "\u200B\u200C\n" +
                "\n" +
                "“呜呃...?!♥”\n" +
                "\n" +
                "突然挨了一记鸡巴巴掌的伊芙琳一脸茫然地捂住脸颊，似乎没理解发生了什么。\u200D\u200C\n" +
                "\n" +
                "然后她可能是稍晚才反应过来状况，脸蛋瞬间涨得通红：\n" +
                "\n" +
                "“呃？！...呜诶？！...呜...？啊呜？...这、这个...怎么办啊！...比...比想象中...你...你的...太大了啦！”\n" +
                "\n" +
                "她咬着下嘴唇瞪视我的鸡巴，突然双手张开丈量起长度，接着把手按在自己小腹上比划：\n" +
                "\n" +
                "'疯、疯、疯了吧！...真要进到这里面？...会、会死的吧？...啊...根本不可能进去的..'\n" +
                "\n" +
                "抱着脑袋疯狂摇头的伊芙琳突然握紧拳头下定决心：\n" +
                "\n" +
                "\"呜呜...！...来吧！伊芙琳！你可以的！都到这一步了怎么能退缩！！\"\n" +
                "\n" +
                "终于做好心理建设的她脱下白色连衣裙。\n" +
                "\n" +
                "唰——\n" +
                "\n" +
                "藏在裙摆下的内衣是前所未见的款式。\n" +
                "\n" +
                "完全无视遮蔽功能的纯黑蕾丝内衣，根本就是专为诱惑男性存在的设计。想到平时总穿小熊图案内裤的伊芙琳专门为我换上这个，难以形容的背德感瞬间涌了上来。\n" +
                "\n" +
                "\"呜噫！！...什、什么啊！...怎、怎么...突然...变得更大了！？\"\n" +
                "\n" +
                "看到完全勃起的肉棒，伊芙琳的瞳孔剧烈颤动。\n" +
                "\n" +
                "\"...喂。该、该不会进到里面还会涨吧？\"\n" +
                "\n" +
                "但她显然没打算停手，直接跨坐到我腿上把内裤拨到一旁。\n" +
                "\n" +
                "小时候一起洗过澡，其实不是第一次看见她的小穴。突然想起来，当时我们还好奇地互相摸过对方性器官呢。\n" +
                "\n" +
                "...但那时和现在的状况实在差太多了。\n" +
                "\n" +
                "唰——\n" +
                "\n" +
                "时隔多年再见到的粉红色小穴正在淅淅沥沥流淌着爱液。\n" +
                "\n" +
                "\"呼...呼呼...要、要慢慢来...慢慢来...\"\n" +
                "\n" +
                "她用双手微微掰开阴唇，抬起腰把龟头抵在入口。\n" +
                "\n" +
                "\"哈啊♥..顶、顶到了..！\"\n" +
                "\n" +
                "现在只要她沉下腰，就要和15年老友真正结合了。\n" +
                "\n" +
                "『这样真的可以吗？』\n" +
                "\n" +
                "...这种想法根本不存在。\n" +
                "\n" +
                "nBc8HTbeyROr96awlD67zwMcic2Pw6y2xiaCxbCNPf0=\n" +
                "\n" +
                "催情药似乎终于起效，满脑子只剩下「再不插进去就要爆炸了」的念头。\n" +
                "\n" +
                "唰— 唰—\n" +
                "\n" +
                "\"嗯♥...呜嗯！...哈啊♥...呜呃...呀！♥\"\n" +
                "\n" +
                "但伊芙琳完全不懂我的煎熬，只顾反复用龟头磨蹭穴口。\n" +
                "\n" +
                "\"哈啊♥...哈啊♥...伊安！...呜呣！...哈♥...好、好舒服♥...伊安...呜嗯..♥\"\n" +
                "\n" +
                "甚至自娱自乐地玩起乳头，发出色气的呻吟。\n" +
                "\n" +
                "『...啊！真的忍不了了！！』\n" +
                "\n" +
                "龟头被摩擦得快要发疯，看着她在眼前一边喊我名字一边自慰，鸡巴真的要爆炸了。\n" +
                "\n" +
                "——猛地！\n" +
                "\n" +
                "\"呜、呜噫！干、干什么！伊、伊安你醒着...呜哇啊！♥\"\n" +
                "\n" +
                "这就是性欲的力量吗。\n" +
                "\n" +
                "催眠草的效果不知去向，身体涌出平时数倍的力量。我瞬间把伊芙琳推倒，抓住双腿大大分开。\n" +
                "\n" +
                "\"呜呃..！♥等、等一下...！稍等...！.等一下啊！♥...伊安！..我、我知道了啦...呜诶！♥...伊安...等一下..！！♥\"\n" +
                "\n" +
                "她的抗议声完全传不进耳朵。\n" +
                "\n" +
                "现在脑海里只有一个念头：\n" +
                "\n" +
                "『插进去！！』\n" +
                "\n" +
                "抓住她纤细的腰肢一口气捅到最深处。\n" +
                "\n" +
                "噗嗤—！\n" +
                "\n" +
                "远超想象的快感席卷全身。\n" +
                "\n" +
                "伊芙琳的小穴又热又紧，简直像是为我量身定做般完美包裹着肉棒。\n" +
                "\n" +
                "\"咕呃..！！！\"\n" +
                "\n" +
                "当龟头撞上宫颈的瞬间，从尾椎窜上的快感沿着脊椎直冲脑门轰然炸开。\n" +
                "\n" +
                "咕嘟— 咕嘟—\n" +
                "\n" +
                "实现将种子播撒给雌性的原始欲望这一念头，让积攒到极限的精液瞬间喷涌而出。\n" +
                "\n" +
                "\"哈啊..\"\n" +
                "\n" +
                "这样持续射精一段时间后，似乎连大脑都充斥的精液略有消退，伊芙琳的身影映入眼帘。\n" +
                "\n" +
                "\"呼..!呜呃...!♥哈啊...哈..♥伊、伊安...稍、稍等♥...呜噫!...别..别停♥...再戳的话...♥要坏掉!...要坏掉啦..♥呜咕...哈..呜嗯!♥...不行...不行...呃嗯!♥♥\"\n" +
                "\n" +
                "但这不意味着会停下。\n" +
                "\n" +
                "\"呜诶!♥..不、不要!!...♥已、已经..不行.!♥子、子宫...要被灌满啦..!!♥...噢!..啊!..♥...呜、呜噫!.♥..啊..啊啊!..♥...去了!...又、又要去啦!♥呃嗯!!\"\n" +
                "\n" +
                "反而当精液充满脑髓时未能看清的、伊芙琳气喘吁吁的模样，此刻令人更想享受这一刻。\n" +
                "\n" +
                "\"呜咕...♥哈...呃嗯...哈呜..!..哈呜嗯!...♥不..不是说不要了吗!!..呃嗯!!♥ 噢噢♥..呜...呜诶..♥...呃..呃嗯...已、已经..够了吧..♥...呜啊嗯..停下..啊!..啊啊啊!♥\"\n" +
                "\n" +
                "\"最后一次! 再做一次就射!\"\n" +
                "\n" +
                "\"啊!.不要!!♥...要、要去啦!!!..高潮啦!!.♥...要..要升天啦♥..被填满啦!!..♥求、求你了!呜嗯♥...呜...呜呼...啊..啊..不要呀!♥..哈.!.呜呼!♥脑、脑袋..变得奇怪!..呜嗯!!..呃呃嗯!!!♥\"\n" +
                "\n" +
                "\"去了!...要去了!\"\n" +
                "\n" +
                "\"──♥♥♥!!\"\n" +
                "\n" +
                "咕嘟! 咕嘟!\n" +
                "\n" +
                "翻着白眼无声尖叫的伊芙琳被我全力抱紧，将睾丸里残留的最后精液尽数注入。\n" +
                "\n" +
                "与此同时...\n" +
                "\n" +
                "『啊...』\n" +
                "\n" +
                "或许是因为超越极限的快感，视野突然模糊，无可抗拒的睡意笼罩了我。\n" +
                "\n" +
                "***\n" +
                "\n" +
                "\n" +
                "\n" +
                "\"哈啊!\"\n" +
                "\n" +
                "次日猛地从床上惊醒环顾四周。\n" +
                "\n" +
                "既不见伊芙琳身影，曾被精液爱液浸透的床铺也整理得一干二净。\n" +
                "\n" +
                "\"是梦吗?\"\n" +
                "\n" +
                "但若要说声「啊原来只是梦」就翻篇，记忆又过于鲜明。\n" +
                "\n" +
                "羞红着脸隔着内裤抚弄我阴茎的伊芙琳。\n" +
                "\n" +
                "用龟头在穴口研磨许久焦躁难耐的伊芙琳。\n" +
                "\n" +
                "c1hQbkVZV1ZObkNXNEI4WjRFQ0lwS1VBMklsMEpmcDFDSUEzUU4weHhieFFIQ0wrK3RXWEJKN3FhWWVZemNRTg\n" +
                "\n" +
                "嘴上说着不要却在每次射精时紧抱我收缩阴道的伊芙琳。\n" +
                "\n" +
                "全都历历在目。\n" +
                "\n" +
                "\"...嗯?\"\n" +
                "\n" +
                "此刻，桌上一张便条闯入视线。\n" +
                "\n" +
                "伊芙琳工整的字迹写着：\n" +
                "\n" +
                "\"开玩笑?我特意做的蛋糕居然吃着吃着就睡着了?下次见面你就死定了!!\"\n" +
                "\n" +
                "就像什么都没发生过一样的伊芙琳的便条，让我感觉像是脑袋被人打了一拳。\n" +
                "\n" +
                "\"...这是什么啊？难道是我做得太狠让她反感了？所以才要当没发生过？\"\n" +
                "\n" +
                "现在回想起来确实有点过分了。\n" +
                "\n" +
                "连伊芙琳哀求着让她稍微休息一下都没理会，只顾着使劲往她腰里捅。\n" +
                "\n" +
                "但第一次尝试性交的我在吃了兽人催情药的状态下怎么可能把持得住啊。\n" +
                "\n" +
                "正郁闷地叹着气时，眼前的论坛突然闪现出来，仿佛能读心似的。\n" +
                "\n" +
                "上一章\n" +
                "下一章\n" +
                "回到主页\n";

//        // 示例Long数字
//        Long number = 297L;
//        // 调用解析方法，生成新的文本
//        String modifiedText = modifier.parse(exampleText, number);
//        System.out.println("解析后文本：\n" + modifiedText + "\n");

        // 调用反解析方法，从新文本中提取数字
        String extractedNumber = modifier.unparse(exampleText);
        System.out.println("反解析得到的数字：" + extractedNumber);

//        // 验证结果，现在解析和反解析的数字顺序应该是一致的
//        System.out.println("\n验证结果：原始数字和反解析的数字是否一致？ " + number.equals(extractedNumber));
//
//        System.out.println("----------------------------------------");
//        System.out.println("演示新添加的 processedLines 方法：\n");
//
//        // 使用 modifiedText 作为输入，来演示被修改的行不会被提取
//        System.out.println("使用解析后的文本作为输入，并添加一些额外行...\n");
//        String demoContent = modifiedText + "\nLine 51\nLine 52\n<img src='test.jpg'>\nLine 54\nLine 55";
//
//        ProcessingResult result = modifier.processedLines(demoContent);
//        System.out.println(new Gson().toJson(result));
    }
}