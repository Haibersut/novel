package com.wtl.novel.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.wtl.novel.CDO.TerminologyModifyCTO;
import com.wtl.novel.CDO.UserTerminologyModifyCTO;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonEscapeUtils {
    private static final Map<Character, String> ESCAPE_MAP = new HashMap<>();

    static {
        // 定义需要转义的字符及其对应的JSON转义序列
        ESCAPE_MAP.put('"', "\\\"");
        ESCAPE_MAP.put('\\', "\\\\");
        ESCAPE_MAP.put('/', "\\/");
        ESCAPE_MAP.put('\b', "\\b");
        ESCAPE_MAP.put('\f', "\\f");
        ESCAPE_MAP.put('\n', "\\n");
        ESCAPE_MAP.put('\r', "\\r");
        ESCAPE_MAP.put('\t', "\\t");
    }

    public static String escapeJsonString(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("\"","＂");
    }

    public static String escapeJsonString1(String input) {
        if (input == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (ESCAPE_MAP.containsKey(c)) {
                // 处理已知转义字符
                sb.append(ESCAPE_MAP.get(c));
            } else if (c < 0x20) {
                // 处理其他控制字符（ASCII < 0x20）
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                // 无需转义的字符
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static Map<String, String> getTable(String json) {
        // 第一步：提取table对象内容的正则
        // 改进后的正则表达式
        String tableRegex =
                "\"table\"\\s*:\\s*\\{([\\s\\S]*?)\\}\\s*(?=,?\\s*(\"translation\"|\\}))";
        Pattern pattern = Pattern.compile(tableRegex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);
        Map<String, String> tableMap = new HashMap<>();
        if (matcher.find()) {
            // 修复提取的JSON片段
            String tableContent = matcher.group(1).trim();
            String validJson = "{ " + tableContent + " }"; // 包裹成完整对象

            // 转换为Map
            Gson gson = new Gson();
            tableMap = gson.fromJson(validJson,
                    new com.google.gson.reflect.TypeToken<Map<String, String>>(){}.getType());
        }
        return tableMap;
    }

    public static String getTranslation1(String json) {
        // 正则匹配 "translation": "..." 到最后一个 } 之间的内容
        Pattern pattern = Pattern.compile(
                "\"translation\"\\s*:\\s*\"([^\"]*)\"\\s*\\}"  // 匹配 "translation": "..." }
        );
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            // 提取内容并处理转义字符
            return matcher.group(1)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
        }
        return ""; // 未匹配到内容时返回空字符串
    }

    public static void main(String[] args) {
        getTranslation("{\n" +
                "  \"table\": {\"지방의대\":\"地方医大\",\"선악과\":\"善恶果\",\"에덴동산\":\"伊甸园\",\"불법하우스\":\"非法赌场\",\"묫자리\":\"坟位\",\"정킷방\":\"百家乐赌场\",\"파칭코\":\"柏青哥\",\"주딱\":\"猪渣\"},\n" +
                "  \"translation\": \"封面故事\\n#001\\n1.\\n承担风险是件重要的事。\\n如果亚当和夏娃没有冒险偷吃善恶果。\\n这个世界就会变成没有色情、赌博、霸凌带来的甜美快感，无聊透顶的伊甸园。\\n-砰 嘭嘭砰\\n雨幕中模糊晕开的夜景。\\n我背靠着防坠落围栏，把脚架在栏杆外侧。\\n轮流抽着烟灌着烧酒，手指啪啪敲击智能手机屏幕。\\n[指挥官大人！这次感觉真的很好！要来一发11连抽吗？]\\n每次点击都眼看着最后一笔氪金资源飞速消失。\\n[忠、忠诚！报告。格里芬大队第四分队所属….]\\n[你叫我吗？呜呼呼]\\n[队长！来得太晚了吧！现在才到！]\\n吞掉那笔钱后吐出来的全是稀有、稀有、普通、稀有，还有我不想要的超级稀有。\\n尽是些引发烦躁与愤怒的捆绑销售垃圾货色。\\n即便如此我的拇指还是习惯性地，在这连续的绝望中祈求着希望，按下了11连抽按钮。\\n各领域专家聚在一起，经过“如何才能让用户沦为神憎鬼厌的赌狗毁掉人生？”这种高级别会议商讨后设计的恶毒抽卡模式……\\n连用户的愤怒与绝望都要机械性地加以利用。\\n就这样烧掉了多少资源呢？\\n[心跳！心跳！超级大机会！]\\n最后一次11连，伴随着特效画面闪耀出七彩光芒。\\n只有确定抽出SSR等级时才会显示的特效。\\n“这次真的出了吗？”\\n3秒。\\n一段不算特别长却感觉无比漫长的时间流逝后。\\n[人、人家才不是因为喜欢主人才来的呢？]\\n传来了日本知名声优配音的傲娇女仆语音。\\n有着0.002%极恶概率的Pickup角色的Live2D插画铺满了整个屏幕。\\n“来了，总算出了啊。”\\n就是这个。\\n流淌在脑血管中五彩斑斓的电流与化学信号的集合体。\\n我生命的光，我生命的火焰，我的罪，我的多巴胺。\\n老实说。\\n我根本不知道这个奶子超大的女仆角色是什么设定，有什么性能。\\n一如既往，只在突破渺茫概率之墙时才感受到的灵魂震颤。\\n我渴望的就是这个。\\n沉浸在大脑颤栗的快感中不过片刻。\\n“哈。”\\n不由得发出一声苦笑。\\n这种时候我到底在干什么啊……\\n又不是雨蛙，却凄凄惨惨地淋着雨翻过屋顶围栏，理由只有一个。\\n为了做极端选择。\\n马上要死掉的家伙因为抽卡成功而兴高采烈的模样，连我自己看着都觉得蠢爆了。\\n关掉游戏，喧闹嘈杂的背景音乐也跟着一起消失了。\\n-叮铃 叮铃\\n取而代之的是现实开启了。\\n[未读消息99+]\\n神经质的震动声和屏幕顶端排成一列的通知。\\n是我一直无视着的现实。\\n[哞哞金融] 金正焕顾客，您8月24日的还款金额尚未支付。\\n[速达贷款] 金正焕先生，今天再不汇款立刻启动资产扣押。这可不是开玩笑的。\\n[来电号码限制] 金正焕你个狗崽子接电话。说好卖了还钱？你他妈真想死吗？\\n[快钱资本] 再这样拖延下去，我们会直接找上您的工作单位。也只好联系您的父母了。\\n这都几点了还在催债？\\n放高利贷的混蛋们真以为自己凌驾于法律之上。\\n“那可真是对不住啦。”\\n我扔掉了嗡嗡作响吵死人的手机。\\n手机在片刻后伴随着“主人混蛋！我一定要报仇！”般的大声响彻底摔碎了。\\n希望你别太怨恨。\\n主人马上也要跟着你去了。\\n“…….”\\n怎么会变成这样呢？\\n是从哪里开始出错的呢？\\n是厌倦了赌场、百家乐赌场、柏青哥、非法赌场后，开始碰加密货币期货的时候？\\n还是用那笔起步资金赚了800亿巨款的时候？\\n或者是凭借冷静理性分析得出的完美空头仓位，被美国猪渣的一条推特搞成坟位的时候？\\n又或者是在那个预备坟位不仅押上了全部财产，还为了补上亏损的本金而借了全额贷款的时候？\\n不对。\\n‘正焕先生的问题……不止是赌博成瘾这一个。’\\n‘赌博、酒精、香烟、概率型游戏、甚至投资女性……形式不同但本质都是一样的。’\\n‘大脑的奖励回路出故障了。大脑已经过度依赖多巴胺这种奖励信号。’\\n‘因此导致了在关注领域之外的所有行为上注意力缺失、冲动控制障碍以及各种成瘾现象。因为无法从普通刺激中获得满足，所以不断寻求新的刺激。’\\n‘简单来说，和吸毒成瘾者的大脑状态几乎差不多。’\\n‘已经到了靠自身力量无法停止的状态。急需住院治疗。’\\n‘…那是不是去吸毒就行了？疯了吗？’\\n还以为是地方医大出身庸医的推销话术没当回事……\\n结果还是搞成了这副模样。\\n“哎呦。”\\n停下这丢人现眼的后悔，从原地站起身来。\\n高层建筑的楼顶风势很猛。\\n只是往下看了一眼，就被那令人眩晕的俯视角弄得头晕目眩。\\n真他妈高啊这个。\\n这种高度的话，应该不会半死不活地让债主有机会来医院探病了。\\n距离如此渴望的摆脱债主生活只剩几步之遥的时刻。\\n-叮铃\\n通知音又响了。\\n“妈的。”\\n最后了到最后还有催债短信？\\n我好歹也有良心，起初倒也不是完全没有愧疚之心……\\n但被这没爹没娘没完没了的凶狠催债折磨了大概三个月，脏话不由自主就蹦出来了。\\n“……？”\\n话说回来手机刚才不是扔了吗？\\n怎么可能还听得到通知声？\\n“什么东西啊这是。”\\n怀着疑问看向正前方，只见眼前半透明的窗口上。\\n[ 1.奋力跳下去 ]\\n[ 2.跳进那个方框里 ]\\n金色的文字正在闪闪发光。\\n就像游戏里选择选项一样。\\n“这什么啊。”\\n是空腹灌太多烧酒了吗？\\n还是快死的时候连各种杂念都不够，甚至开始出现幻觉了？\\n但是就算揉揉眼睛用力摇头。\\n选项依然固定在中央，轻飘飘地浮动着，像是在催促快点选择它。\\n“…说是方框里面？”\\n-呼咿咿咿！\\n“方框…”\n" +
                "}");
    }

    public static String getTranslation(String json) {
        // 正则表达式：匹配从"translation":到最后一个}之间的内容
        String regex =
                "\"translation\"\\s*:\\s*\" " + // 匹配键和冒号
                        "((?s).*?)" +                   // 匹配所有字符（包括换行）
                        "\"\\s*}" +                     // 匹配结束引号和对象闭合符
                        "(?=\\s*$)" ;                   // 确保是JSON的最后一个}

        Pattern pattern = Pattern.compile(regex, Pattern.COMMENTS);
        Matcher matcher = pattern.matcher(json);
        StringBuffer sb = new StringBuffer();
        if (matcher.find()) {
            // 提取内容并还原转义字符
            String content = matcher.group(1)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
            sb.append(content);
        }
        String string = sb.toString();
        if (string.isEmpty()) {
            string = JsonEscapeUtils.getTranslation1(json);
        }
        return string;
    }

    public static String terminologyModifyToJson(TerminologyModifyCTO obj) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }

    public static String terminologyModifyToJson(UserTerminologyModifyCTO obj) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(obj);
    }

}