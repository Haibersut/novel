package com.wtl.novel.Service;

import com.wtl.novel.entity.UserFeedback;
import com.wtl.novel.repository.UserFeedbackRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFeedbackService {
    private final UserFeedbackRepository userFeedbackRepository;

    public UserFeedbackService(UserFeedbackRepository userFeedbackRepository) {
        this.userFeedbackRepository = userFeedbackRepository;
    }

    public UserFeedback createFeedback(UserFeedback feedback) {
        return userFeedbackRepository.save(feedback);
    }

    public List<UserFeedback> findByNovelIdAndChapterIdAndIsDeleteFalse(Long novelId, Long chapterId) {
        return userFeedbackRepository.findByNovelIdAndChapterIdAndIsDeleteFalse(novelId, chapterId);
    }

    public Page<UserFeedback> getAllFeedbacks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userFeedbackRepository.findAll(pageable);
    }

    public Page<UserFeedback> getFeedbacksByStatus(Boolean isResolved, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userFeedbackRepository.findByIsResolved(isResolved, pageable);
    }

    public UserFeedback updateFeedbackStatus(Long id, Boolean isResolved) {
        UserFeedback feedback = userFeedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        feedback.setIsResolved(isResolved);
        return userFeedbackRepository.save(feedback);
    }
}