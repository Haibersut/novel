package com.wtl.novel.repository;

import com.wtl.novel.DTO.UserIdUsernameDTO;
import com.wtl.novel.entity.UserChapterEdit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserChapterEditRepository extends JpaRepository<UserChapterEdit, Long> {

    /**
     * 根据用户+章节查找唯一编辑记录
     */
    Optional<UserChapterEdit> findByUserIdAndChapterId(Long userId, Long chapterId);

    @Query("select new com.wtl.novel.DTO.UserIdUsernameDTO(u.userId, u.username) " +
            "from UserChapterEdit u " +
            "where u.chapterId = :chapterId")
    List<UserIdUsernameDTO> findUserIdAndUsernameByNovelIdAndChapterId(
            @Param("chapterId") Long chapterId);


    @Modifying
    @Query("update UserChapterEdit u set u.username = :newName where u.userId = :userId")
    int updateUsernameByUserId(@Param("userId") Long userId,
                               @Param("newName") String newName);
}