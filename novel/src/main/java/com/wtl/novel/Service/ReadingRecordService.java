package com.wtl.novel.Service;

import com.wtl.novel.entity.ReadingRecord;
import com.wtl.novel.repository.ReadingRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReadingRecordService {
    @Autowired
    private ReadingRecordRepository readingRecordRepository;

    public List<ReadingRecord> getReadingRecordsByBookIds(Long userId, List<Long> bookIds) {
        return readingRecordRepository.findByUserIdAndNovelIdIn(userId, bookIds);
    }

    public void updateReadingRecord(Long userId, Long novelId, Long lastChapter, Long lastChapterId) {
        ReadingRecord record = readingRecordRepository.findByUserIdAndNovelId(userId, novelId);
        if (record == null) {
            record = new ReadingRecord();
            record.setUserId(userId);
            record.setNovelId(novelId);
            record.setUpdateTime(new Date());
        }
        record.setUpdateTime(new Date());
        record.setLastChapter(lastChapter);
        record.setLastChapterId(lastChapterId);
        readingRecordRepository.save(record);
    }

}