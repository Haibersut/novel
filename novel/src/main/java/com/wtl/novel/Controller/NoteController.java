package com.wtl.novel.Controller;

import com.wtl.novel.CDO.DeleteRequestCTO;
import com.wtl.novel.CDO.NoteCTO;
import com.wtl.novel.CDO.NoteGroupByChapter;
import com.wtl.novel.CDO.NoteGroupByChapterWithNovelTitle;
import com.wtl.novel.Service.CredentialService;
import com.wtl.novel.Service.NoteService;
import com.wtl.novel.entity.Credential;
import com.wtl.novel.entity.Note;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;
    @Autowired
    private CredentialService credentialService;
    // 添加笔记
    @PostMapping("/add")
    public ResponseEntity<String> addNote(@RequestBody Note note, HttpServletRequest httpRequest) {
        String authorization = httpRequest.getHeader("Authorization");
        if (authorization == null) {
            return null;
        }
        String[] authorizationInfo = authorization.split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        note.setUserId(credential.getUser().getId());
        if (noteService.addNote(note)) {
            return ResponseEntity.ok("提交完成！");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("提交失败！");
    }

    // 删除笔记（逻辑删除）
    @PostMapping("/delete")
    public ResponseEntity<?> deleteNotes(@RequestBody DeleteRequestCTO request, HttpServletRequest httpRequest) {
        String authorization = httpRequest.getHeader("Authorization");
        if (authorization == null) {
            return null;
        }
        String[] authorizationInfo = authorization.split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        noteService.deleteByIds(request.getIds(), credential.getUser().getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    // 获取所有笔记
//    @GetMapping
//    public List<Note> getAllNotes() {
//        return noteService.getAllNotes();
//    }

    // 获取单个笔记
//    @GetMapping("/{id}")
//    public Optional<Note> getNoteById(@PathVariable Long id) {
//        return noteService.getNoteById(id);
//    }
//
    // 根据用户ID获取笔记
    @GetMapping("/get")
    public List<NoteCTO> getNotes(HttpServletRequest httpRequest) {
        String authorization = httpRequest.getHeader("Authorization");
        if (authorization == null) {
            return null;
        }
        String[] authorizationInfo = authorization.split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        return noteService.getNovelIdsByUserId(credential.getUser().getId());
    }

    // 根据小说ID获取笔记
//    @GetMapping("/novel/{novelId}")
//    public List<Note> getNotesByNovelId(@PathVariable Long novelId) {
//        return noteService.getNotesByNovelId(novelId);
//    }
//    // 根据小说ID获取笔记
//    @GetMapping("/search/{novelId}")
//    public List<Note> searchNotesByNovelId(@PathVariable Long novelId, @Param("noteContent") String noteContent) {
//        return noteService.searchNotesByNovelId(novelId, noteContent);
//    }


    // 根据章节ID获取笔记
    @GetMapping("/chapter/{chapterId}")
    public List<Note> getNotesByChapterId(@PathVariable Long chapterId,HttpServletRequest httpRequest) {
        String authorization = httpRequest.getHeader("Authorization");
        if (authorization == null) {
            return null;
        }
        String[] authorizationInfo = authorization.split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        if (credential == null || credential.getExpiredAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        return noteService.getNotesByUserAndChapter(credential.getUser().getId(), chapterId);
    }

    @GetMapping("/groupByChapter/{novelId}")
    public NoteGroupByChapterWithNovelTitle getNotesGroupedByChapter(
            @PathVariable Long novelId, HttpServletRequest httpRequest) {
        String[] authorizationInfo = httpRequest.getHeader("Authorization").split(";");
        String authorizationHeader = authorizationInfo[0];
        Credential credential = credentialService.findByToken(authorizationHeader);
        return noteService.getNotesGroupedByChapter(credential.getUser().getId(), novelId);
    }
}