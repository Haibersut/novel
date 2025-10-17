package com.wtl.novel.CDO;

import com.wtl.novel.entity.Terminology;
import java.io.Serializable;
import java.util.List;

public class TerminologyModifyCTO implements Serializable {
    private Long novelId;
    private Long chapterId;
    private List<Long> paginatedToDeleteTerms;
    private List<Terminology> paginatedToModifyTerms;

    public TerminologyModifyCTO(Long novelId, Long chapterId, List<Long> paginatedToDeleteTerms, List<Terminology> paginatedToModifyTerms) {
        this.novelId = novelId;
        this.chapterId = chapterId;
        this.paginatedToDeleteTerms = paginatedToDeleteTerms;
        this.paginatedToModifyTerms = paginatedToModifyTerms;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public Long getNovelId() {
        return novelId;
    }

    public void setNovelId(Long novelId) {
        this.novelId = novelId;
    }

    public List<Long> getPaginatedToDeleteTerms() {
        return paginatedToDeleteTerms;
    }

    public void setPaginatedToDeleteTerms(List<Long> paginatedToDeleteTerms) {
        this.paginatedToDeleteTerms = paginatedToDeleteTerms;
    }

    public List<Terminology> getPaginatedToModifyTerms() {
        return paginatedToModifyTerms;
    }

    public void setPaginatedToModifyTerms(List<Terminology> paginatedToModifyTerms) {
        this.paginatedToModifyTerms = paginatedToModifyTerms;
    }
}
