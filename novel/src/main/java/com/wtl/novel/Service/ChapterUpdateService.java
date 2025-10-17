package com.wtl.novel.Service;

import com.wtl.novel.entity.ChapterUpdate;
import com.wtl.novel.repository.ChapterUpdateRepository;
import com.wtl.novel.repository.InvitationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChapterUpdateService {
    @Autowired
    private ChapterUpdateRepository chapterUpdateRepository;

    public boolean updateChapterContent(ChapterUpdate request) {
        ChapterUpdate savedEntity = chapterUpdateRepository.save(request);
        return savedEntity.getId() != null;
    }
}
