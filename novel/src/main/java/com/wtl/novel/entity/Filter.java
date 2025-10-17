package com.wtl.novel.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Filter {

    /**
     * 把 List<NpPlatform> 里每个 url 置 null，
     * 再把 Filter 下所有 Map<*,String> 的 value 置 null
     */
    public static void cleanConfig(List<NpPlatform> list) {
        list.forEach(p -> {
            p.setUrl(null);   // 1. url 清掉
            Optional.ofNullable(p.getFilter()).ifPresent(f -> {
                // 2. 所有 Map 型字段的 value 设 null
                f.setSort(nullableMap(f.getSort()));
                f.setTime(nullableMap(f.getTime()));
                f.setLimit(nullableMap(f.getLimit()));
                f.setTag(nullableMap(f.getTag()));
                f.setPlus(nullableMap(f.getPlus()));
                f.setUpdate(nullableMap(f.getUpdate()));
            });
        });
    }

    /* 工具：把原 Map 所有 value 置 null，key 保留 */
    private static <K> Map<K, String> nullableMap(Map<K, String> src) {
        if (src == null) return null;
        src.replaceAll((k, v) -> null);   // 原地替换
        return src;
    }

    private Map<String, String> sort;
    private Map<String, String> time;
    private Map<String, String> limit;
    private Map<String, String> tag;
    private Map<String, String> plus;   // 可选
    private Map<String, String> update; // 可选

    /* ====== getter / setter ====== */
    public Map<String, String> getSort() {
        return sort;
    }
    public void setSort(Map<String, String> sort) {
        this.sort = sort;
    }

    public Map<String, String> getTime() {
        return time;
    }
    public void setTime(Map<String, String> time) {
        this.time = time;
    }

    public Map<String, String> getLimit() {
        return limit;
    }
    public void setLimit(Map<String, String> limit) {
        this.limit = limit;
    }

    public Map<String, String> getTag() {
        return tag;
    }
    public void setTag(Map<String, String> tag) {
        this.tag = tag;
    }

    public Map<String, String> getPlus() {
        return plus;
    }
    public void setPlus(Map<String, String> plus) {
        this.plus = plus;
    }

    public Map<String, String> getUpdate() {
        return update;
    }
    public void setUpdate(Map<String, String> update) {
        this.update = update;
    }
}