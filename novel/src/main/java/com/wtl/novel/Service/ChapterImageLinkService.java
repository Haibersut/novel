package com.wtl.novel.Service;

import com.wtl.novel.entity.ChapterImageLink;
import com.wtl.novel.repository.ChapterImageLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChapterImageLinkService {

    @Autowired
    private ChapterImageLinkRepository chapterImageLinkRepository;

    // 保存章节图片链接
    @Transactional
    public List<ChapterImageLink> saveChapterImageLink(List<ChapterImageLink> chapterImageLinkList) {
        return chapterImageLinkRepository.saveAll(chapterImageLinkList);
    }

    public List<ChapterImageLink> findByChapterTrueId(String chapterTrueId) {
        return chapterImageLinkRepository.findByChapterTrueId(chapterTrueId);
    }

    public void deleteAll(List<ChapterImageLink> chapterImageLinkList) {
        chapterImageLinkRepository.deleteAll(chapterImageLinkList);
    }
}