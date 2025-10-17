package com.wtl.novel.repository;

import com.wtl.novel.DTO.PostScoreDto;
import com.wtl.novel.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Modifying
    @Query("update Post p set p.author = :newAuthor where p.userId = :userId")
    int updateAuthorByUserId(@Param("userId") Long userId,
                             @Param("newAuthor") String newAuthor);
    // 根据 userId 分页查询
    Page<Post> findByUserId(Long userId, Pageable pageable);
    // 根据 postType 查询并分页排序
    @Query("SELECT p FROM Post p WHERE p.postType = :postType")
    Page<Post> findByPostType(@Param("postType") Integer postType, Pageable pageable);

    // 根据 postType 查询并分页排序
    @Query("SELECT p FROM Post p WHERE p.collections = :novelId")
    Page<Post> findByPostTypeAndNovelId(@Param("novelId") Long novelId, Pageable pageable);

    List<Post> findByPostType(Integer postType);
    // 根据 ID 将 commentNum 加 1
    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.commentNum = p.commentNum + 1 WHERE p.id = :id")
    void incrementCommentNumById(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("update Post p set p.agree = p.agree + 1 where p.id = :postId")
    void incrAgree(@Param("postId") Long postId);

    // 点赞 -1
    @Transactional
    @Modifying
    @Query("update Post p set p.agree = p.agree - 1 where p.id = :postId")
    void decrAgree(@Param("postId") Long postId);

    // 反对 +1
    @Transactional
    @Modifying
    @Query("update Post p set p.disagree = p.disagree + 1 where p.id = :postId")
    void incrDisagree(@Param("postId") Long postId);

    // 反对 -1
    @Transactional
    @Modifying
    @Query("update Post p set p.disagree = p.disagree - 1 where p.id = :postId")
    void decrDisagree(@Param("postId") Long postId);


    @Query("""
    select new com.wtl.novel.DTO.PostScoreDto(p.agree, p.disagree, p.collections,p.recommended)
    from Post p
    where p.postType = 0
    """)
    List<PostScoreDto> findAllTypeZeroProjection();
}