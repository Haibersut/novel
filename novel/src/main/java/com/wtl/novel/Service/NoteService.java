package com.wtl.novel.Service;

import com.wtl.novel.CDO.NoteCTO;
import com.wtl.novel.CDO.NoteGroupByChapter;
import com.wtl.novel.CDO.NoteGroupByChapterWithNovelTitle;
import com.wtl.novel.entity.Chapter;
import com.wtl.novel.entity.Note;
import com.wtl.novel.entity.Novel;
import com.wtl.novel.repository.ChapterRepository;
import com.wtl.novel.repository.NoteRepository;
import com.wtl.novel.repository.NovelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private NovelRepository novelRepository;
    @Autowired
    private ChapterRepository chapterRepository;

    public List<Note> getNotesByUserAndChapter(Long userId, Long chapterId) {
        return noteRepository.findByUserIdAndChapterId(userId, chapterId);
    }


    public NoteGroupByChapterWithNovelTitle getNotesGroupedByChapter(Long userId, Long novelId) {
        List<Note> notes = noteRepository.findByUserIdAndNovelIdAndIsDeletedFalse(userId, novelId);

        Set<Long> chapterIds = notes.stream()
                .map(Note::getChapterId)
                .collect(Collectors.toSet());

        // 改为使用投影查询
        List<ChapterRepository.ChapterNumberProjection> projections =
                chapterRepository.findChapterNumbersByIds(new ArrayList<>(chapterIds));

        // 构建ID到章节号的映射
        Map<Long, Integer> chapterNumberMap = projections.stream()
                .collect(Collectors.toMap(
                        ChapterRepository.ChapterNumberProjection::getId,
                        ChapterRepository.ChapterNumberProjection::getChapterNumber
                ));

        List<NoteGroupByChapter> collect = notes.stream()
                .collect(Collectors.groupingBy(Note::getChapterId))
                .entrySet().stream()
                .map(entry -> {
                    Integer chapterNumber = chapterNumberMap.get(entry.getKey());
                    return chapterNumber != null ?
                            new NoteGroupByChapter(
                                    chapterNumber,
                                    entry.getKey(), // 保留chapterId
                                    entry.getValue()
                            ) : null;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(NoteGroupByChapter::getChapterNumber))
                .toList();
        Novel byIdAndIsDeletedFalse = novelRepository.findByIdAndIsDeletedFalse(novelId);
        return new NoteGroupByChapterWithNovelTitle(byIdAndIsDeletedFalse.getTitle(), notes.size(), collect);
    }

    public List<NoteCTO> getNovelIdsByUserId(Long userId) {
        List<Long> novelIdsByUserId = noteRepository.findNovelIdsByUserId(userId);
        List<Novel> novels = novelRepository.findByIdInAndIsDeletedFalse(novelIdsByUserId);

        return novels.stream()
                .map(novel -> new NoteCTO(novel.getId(), novel.getTitle()))
                .collect(Collectors.toList());
    }

    // 添加笔记
    public Boolean addNote(Note note) {
        Long chapterId = note.getChapterId();
        Chapter byIdAndIsDeletedFalse = chapterRepository.findByIdAndIsDeletedFalse(chapterId);
        if (byIdAndIsDeletedFalse.getContent().length() < note.getContent().length()) {
            return false;
        }
        try {
            noteRepository.save(note);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    // 更新笔记
    public Note updateNote(Long id, Note noteDetails) {
        return noteRepository.findById(id)
                .map(note -> {
                    note.setContent(noteDetails.getContent());
                    note.setChapterId(noteDetails.getChapterId());
                    note.setNovelId(noteDetails.getNovelId());
                    note.setUserId(noteDetails.getUserId());
                    return noteRepository.save(note);
                })
                .orElseThrow(() -> new RuntimeException("Note not found"));
    }

    public void deleteByIds(List<Long> ids, Long userId) {
        noteRepository.deleteByIds(ids, userId);
    }

    // 删除笔记（逻辑删除）
    public void deleteNote(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        note.setIsDeleted(true);
        noteRepository.save(note);
    }

    // 获取所有笔记
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    // 获取单个笔记
    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }

    // 根据用户ID获取笔记
    public List<Note> getNotesByUserId(Long userId) {
        return noteRepository.findByUserId(userId);
    }

    // 根据小说ID获取笔记
    public List<Note> getNotesByNovelId(Long novelId) {
        return noteRepository.findByNovelId(novelId);
    }

    public List<Note> searchNotesByNovelId(Long novelId, String noteContent) {
        return noteRepository.findByNovelIdAndContentLike(novelId, noteContent);
    }

    // 根据章节ID获取笔记
    public List<Note> getNotesByChapterId(Long chapterId) {
        return noteRepository.findByChapterIdAndIsDeletedFalse(chapterId);
    }
}