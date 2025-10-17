package com.wtl.novel.Service;

import com.wtl.novel.DTO.ChapterSimpleInfo;
import com.wtl.novel.entity.ChapterErrorExecute;
import com.wtl.novel.repository.ChapterErrorExecuteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChapterErrorExecuteService {

    @Autowired
    private ChapterErrorExecuteRepository chapterErrorExecuteRepository;
    

    public List<ChapterErrorExecute> getAllChapterErrorExecutes() {
        return chapterErrorExecuteRepository.findAllByIsDeletedFalse();
    }

    public List<ChapterSimpleInfo> findTitlesByNovelIdAndIsDeletedFalseAndNowStateNot(Long novelId) {
        return chapterErrorExecuteRepository.findTitlesAndChapterNumbersByNovelId(novelId);
    }

    
    public List<ChapterErrorExecute> getChapterErrorExecutesByNovelId(Long novelId) {
        return chapterErrorExecuteRepository.findAllByNovelIdAndIsDeletedFalse(novelId);
    }



    
    public ChapterErrorExecute saveChapterErrorExecute(ChapterErrorExecute ChapterErrorExecute) {
        return chapterErrorExecuteRepository.save(ChapterErrorExecute);
    }

    
    public void deleteChapterErrorExecute(Long id) {
        chapterErrorExecuteRepository.deleteById(id);
    }

    
    public List<String> getChapterTitlesByNovelId(Long novelId) {
        return chapterErrorExecuteRepository.findTitlesByNovelIdAndIsDeletedFalse(novelId);
    }
}
