package com.wtl.novel.Service;

import com.wtl.novel.DTO.ChapterSimpleInfo;
import com.wtl.novel.entity.ChapterExecute;
import com.wtl.novel.repository.ChapterExecuteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChapterExecuteService {

    @Autowired
    private ChapterExecuteRepository chapterExecuteRepository;
    

    public List<ChapterExecute> getAllChapterExecutes() {
        return chapterExecuteRepository.findAllByIsDeletedFalse();
    }

    public List<ChapterSimpleInfo> findTitlesByNovelIdAndIsDeletedFalseAndNowStateNot(Long novelId) {
        return chapterExecuteRepository.findTitlesAndChapterNumbersByNovelId(novelId);
    }

    
    public List<ChapterExecute> getChapterExecutesByNovelId(Long novelId) {
        return chapterExecuteRepository.findAllByNovelIdAndIsDeletedFalse(novelId);
    }

    
    public ChapterExecute getChapterExecuteByNovelIdAndChapterNumber(Long novelId, Integer chapterNumber) {
        return chapterExecuteRepository.findByNovelIdAndChapterNumberAndIsDeletedFalse(novelId, chapterNumber);
    }

    
    public ChapterExecute saveChapterExecute(ChapterExecute chapterExecute) {
        return chapterExecuteRepository.save(chapterExecute);
    }

    
    public void deleteChapterExecute(Long id) {
        chapterExecuteRepository.deleteById(id);
    }

    
    public List<String> getChapterTitlesByNovelId(Long novelId) {
        return chapterExecuteRepository.findTitlesByNovelIdAndIsDeletedFalse(novelId);
    }
}
