package com.wtl.novel.CDO;

import com.wtl.novel.entity.Terminology;
import com.wtl.novel.entity.UserGlossary;

import java.io.Serializable;
import java.util.List;


public class UserTerminologyModifyCTO implements Serializable {
    private Long novelId;
    private List<Long> paginatedToDeleteTerms;
    private List<UserGlossary> paginatedToModifyTerms;

    public UserTerminologyModifyCTO(Long novelId, List<Long> paginatedToDeleteTerms, List<UserGlossary> paginatedToModifyTerms) {
        this.novelId = novelId;
        this.paginatedToDeleteTerms = paginatedToDeleteTerms;
        this.paginatedToModifyTerms = paginatedToModifyTerms;
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

    public List<UserGlossary> getPaginatedToModifyTerms() {
        return paginatedToModifyTerms;
    }

    public void setPaginatedToModifyTerms(List<UserGlossary> paginatedToModifyTerms) {
        this.paginatedToModifyTerms = paginatedToModifyTerms;
    }
}
