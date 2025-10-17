package com.wtl.novel.translator;

import java.util.Map;

public class JsonData {
    private Map<String, String> table;
    private String translation;

    public Map<String, String> getTable() {
        return table;
    }

    public void setTable(Map<String, String> table) {
        this.table = table;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    @Override
    public String toString() {
        return "JsonData{table=" + table + ", translation='" + translation + "'}";
    }
}