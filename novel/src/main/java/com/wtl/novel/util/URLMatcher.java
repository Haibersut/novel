package com.wtl.novel.util;

import com.wtl.novel.filter.RequestFilter;
import java.util.List;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wtl.novel.Service.RequestLogService.CHAPTERS_ID_PATTERN;

public class URLMatcher {

    private static final List<String> PATTERNSSHUTU = Arrays.asList(
            "^/api/shuTu/.*$"
    );

    // 定义正则表达式列表
    private static final List<String> PATTERNS = Arrays.asList(
//            "^/api/platform/[a-zA-Z]+$", // 匹配 /api/platform/字符
//            "^/api/tag/all/[a-zA-Z]+$", // 匹配 /api/tag/all/字符
//            "^/api/novelPia/executeDownloadOne/\\d+$", // 匹配 /api/novels/数字
//            "^/api/chaptersExecute/novel/\\d+$", // 匹配 /api/chaptersExecute/novel/数字
//            "^/api/chapters/getChaptersByNovelId/\\d+$", // 匹配 /api/chapters/getChaptersByNovelId/数字
//            "^/api/tag/getTagsByNovelId/\\d+$", // 匹配 /api/tag/getTagsByNovelId/数字
//            "^/api/favorites/user/\\d+/[a-zA-Z]+$", // 匹配 /api/favorites/user/数字/字符
//            "^/api/novels/getNovelsByPlatform/[^/]+/\\d+/[^/]+/\\d+/\\d+$", // 匹配 /api/novels/getNovelsByPlatform/任意非斜杠字符/数字/任意非斜杠字符/数字/数字
            "^/api/auth/login$", // 匹配 /api/auth
            "^/test/test3$", // 匹配 /api/auth
//            "^/api/posts/getPosts$", // 匹配 /api/posts/getPosts?查询参数
//            "^/api/posts/\\d+$", // 匹配 /api/posts/数字
//            "^/api/notes/chapter/\\d+$", // 匹配 /api/posts/数字
//            "^/api/novels/searchByKeyWord$", // 匹配 /api/posts/数字
//            "^/api/novels/get$", // 匹配 /api/novels/get?查询参数
//            "^/api/dic/getHome$", // 匹配 /api/novels/get?查询参数
            "^/api/novels/executeRecommend", // 匹配 /api/novels/get?查询参数
//            "^/api/chapters/test", // 匹配 /api/novels/get?查询参数
//            "^/api/novelPia/executeTask2", // 匹配 /api/novels/get?查询参数
//            "^/api/novelPia/executeTask3", // 匹配 /api/novels/get?查询参数
            "^/api/novelPia/executeTask5", // 匹配 /api/novels/get?查询参数
            "^/api/novelPia/saveNovelsByTagIdFix" // 匹配 /api/novels/get?查询参数
//            "^/api/novelPia/executeTask7", // 匹配 /api/novels/get?查询参数
//            "^/api/novelPia/executeTask6" // 匹配 /api/novels/get?查询参数
//            "^/api/dic/getNovelDetail$", // 匹配 /api/novels/get?查询参数
//            "^/api/novels/getNovelsByPlatform$", // 匹配 /api/novels/get?查询参数
//            "^/api/terminologies/getTerminologyByPlatform$", // 匹配 /api/novels/get?查询参数
//            "^/api/tag/all/[^/]+/[^/]+$" // 匹配 /api/novels/get?查询参数
    );

    // 定义正则表达式列表
    private static final List<String> ISValid = List.of(
            "^/api/share/upload$", // 匹配 /api/novels/get?查询参数
            "^/api/share/update$", // 匹配 /api/novels/get?查询参数
            "^/api/share/updateNovelDetail/\\d+$" // 匹配 /api/novels/get?查询参数
    );

    public static boolean matchesISValid(String url) {
        // 遍历所有正则表达式，检查是否匹配
        for (String pattern : ISValid) {
            if (Pattern.matches(pattern, url)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matches(String url) {
        // 遍历所有正则表达式，检查是否匹配
        for (String pattern : PATTERNS) {
            if (Pattern.matches(pattern, url)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesShuTu(String url) {
        // 遍历所有正则表达式，检查是否匹配
        for (String pattern : PATTERNSSHUTU) {
            if (Pattern.matches(pattern, url)) {
                return true;
            }
        }
        return false;
    }

}