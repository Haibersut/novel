package com.wtl.novel.CDO;

import java.util.List;

public class DiffDto {

    public DiffDto() {
    }

    public DiffDto(Long novelId, List<String> add, List<Long> delete) {
        this.novelId = novelId;
        this.add = add;
        this.delete = delete;
    }

    private Long novelId;

    private List<String> add;
    private List<Long> delete;

    public Long getNovelId() {
        return novelId;
    }

    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }

    public List<String> getAdd() {
        return add;
    }

    public void setAdd(List<String> add) {
        this.add = add;
    }

    public List<Long> getDelete() {
        return delete;
    }

    public void setDelete(List<Long> delete) {
        this.delete = delete;
    }

    @Override
    public String toString() {
        return "DiffDto{" +
                "novelId=" + novelId +
                ", add=" + add +
                ", delete=" + delete +
                '}';
    }
}