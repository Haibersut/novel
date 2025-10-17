package com.wtl.novel.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtl.novel.CDO.NovelActiveMap;
import com.wtl.novel.entity.Filter;
import com.wtl.novel.entity.NpPlatform;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class NovelpiaUrlBuilder {

    /* 1. 一次性把 JSON 读到内存，后续复用 */
    public static List<NpPlatform> PLATFORMS = null;

    /* 2. 业务入口，参数含义不变 */
    public static String build(NovelActiveMap map) {
        NpPlatform p = PLATFORMS.stream()
                .filter(it -> map.getPlatformName().equals(it.getPlatformName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未知平台：" + map.getPlatformName()));

        String url = p.getUrl();
        Filter f = p.getFilter();
        if (f == null) return url;

        url = url.replace("%sort",  orEmpty(f.getSort(),  map.getSort()))
                .replace("%time",  orEmpty(f.getTime(),  map.getTime()))
                .replace("%limit", orEmpty(f.getLimit(), map.getLimit()))
                .replace("%plus",  orEmpty(f.getPlus(),  map.getPlus()))
                .replace("%update",orEmpty(f.getUpdate(),map.getUpdate()));

        String tagValue = Optional.ofNullable(f.getTag())
                .map(m -> m.get(map.getTag()))
                .orElse("");
        if (!tagValue.isEmpty() && !tagValue.startsWith("?")) {
            tagValue = "?" + tagValue;
        }
        url = url.replace("%tag", tagValue);

        return url;
    }

    private static String orEmpty(Map<String, String> map, String key) {
        if (key == null || key.isBlank()) key = "全部";
        String finalKey = key;
        return Optional.ofNullable(map).map(m -> m.get(finalKey)).orElse("");
    }

    private NovelpiaUrlBuilder() {}
}