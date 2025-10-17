package com.wtl.novel.Service;

import com.wtl.novel.CDO.PostCTO;
import com.wtl.novel.DTO.PostScoreDto;
import com.wtl.novel.entity.Novel;
import com.wtl.novel.entity.Post;
import com.wtl.novel.entity.UserBlacklist;
import com.wtl.novel.repository.NovelRepository;
import com.wtl.novel.repository.PostCommentRepository;
import com.wtl.novel.repository.PostRepository;
import com.wtl.novel.repository.UserBlacklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private UserBlacklistRepository userBlacklistRepository;
    @Autowired
    private NovelRepository novelRepository;

    public void incrementCommentNum(Long postId) {
        postRepository.incrementCommentNumById(postId);
    }

    public Page<PostCTO> getPostsByUserId(Long userId, Pageable pageable) {
        Page<Post> byPostType = postRepository.findByUserId(userId, pageable);

        // 提取 Post 的 collections 字段
        List<Long> idList = byPostType.getContent().stream()
                .map(Post::getCollections)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 查询对应的 Novel
        List<Novel> novels = novelRepository.findByIdInAndIsDeletedFalse(idList);
        // 将 Post 转换为 PostCTO
        List<PostCTO> postCTOs = byPostType.getContent().stream()
                .map(post -> {
                    // 找到对应的 Novel
                    Novel novel = novels.stream()
                            .filter(n -> n.getId().equals(post.getCollections()))
                            .findFirst()
                            .orElse(null);

                    // 创建 PostCTO
                    return new PostCTO(post, novel != null ? novel.getTitle() : null);
                })
                .collect(Collectors.toList());

        // 创建新的 Page<PostCTO>
        return new PageImpl<>(postCTOs, pageable, byPostType.getTotalElements());
    }

    public Page<PostCTO> getPostsByPostType(Long userId, Integer postType, Pageable pageable) {
        Page<Post> byPostType = postRepository.findByPostType(postType, pageable);

        // 提取 Post 的 collections 字段
        List<Long> idList = byPostType.getContent().stream()
                .map(Post::getCollections)
                .collect(Collectors.toList());

        // 查询对应的 Novel
        List<Novel> novels = novelRepository.findByIdInAndIsDeletedFalse(idList);
        List<UserBlacklist> blacklists = userBlacklistRepository.findByUserId(userId);
        Set<Long> list = blacklists.stream().map(UserBlacklist::getBlockedId).collect(Collectors.toSet());
        // 将 Post 转换为 PostCTO
        List<PostCTO> postCTOs = byPostType.getContent().stream()
                .filter(post -> !list.contains(post.getUserId()))
                .map(post -> {
                    // 找到对应的 Novel
                    Novel novel = novels.stream()
                            .filter(n -> n.getId().equals(post.getCollections()))
                            .findFirst()
                            .orElse(null);

                    // 创建 PostCTO
                    return new PostCTO(post, novel != null ? novel.getTitle() : "无名");
                })
                .collect(Collectors.toList());

        // 创建新的 Page<PostCTO>
        return new PageImpl<>(postCTOs, pageable, byPostType.getTotalElements());
    }

    public Page<PostCTO> getAllPostsByNovelId(Long novelId, Pageable pageable) {
        Page<Post> byPostType = postRepository.findByPostTypeAndNovelId(novelId, pageable);

        // 提取 Post 的 collections 字段
        List<Long> idList = new ArrayList<>();
        idList.add(novelId);

        // 查询对应的 Novel
        List<Novel> novels = novelRepository.findByIdInAndIsDeletedFalse(idList);

        // 将 Post 转换为 PostCTO
        List<PostCTO> postCTOs = byPostType.getContent().stream()
                .map(post -> {
                    // 找到对应的 Novel
                    Novel novel = novels.stream()
                            .filter(n -> n.getId().equals(post.getCollections()))
                            .findFirst()
                            .orElse(null);

                    // 创建 PostCTO
                    return new PostCTO(post, novel != null ? novel.getTitle() : "无名");
                })
                .collect(Collectors.toList());

        // 创建新的 Page<PostCTO>
        return new PageImpl<>(postCTOs, pageable, byPostType.getTotalElements());
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public PostCTO getPostById(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null && post.getCollections() != null) {
            Novel byIdAndIsDeletedFalse = novelRepository.findByIdAndIsDeletedFalse(post.getCollections());
            return new PostCTO(post, byIdAndIsDeletedFalse.getTitle());
        }
        assert post != null;
        return new PostCTO(post, "");
    }

    public Post createPost(Post post) {
        post.setAgree(0);
        post.setDisagree(0);
        return postRepository.save(post);
    }

    public Post collectPost(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post != null) {
            post.setCollections(post.getCollections() + 1);
            return postRepository.save(post);
        }
        return null;
    }
// and  p.recommended = true

    public Map<Long, Long> calcScoreByCollections() {
        List<PostScoreDto> posts = postRepository.findAllTypeZeroProjection();

        return posts.stream()
                .filter(p -> p.getCollections() != null)
                .collect(Collectors.groupingBy(
                        PostScoreDto::getCollections,
                        Collectors.summingLong(p -> {
                            int cmp = Integer.compare(p.getAgree(), p.getDisagree());
                            if (p.isRecommended()) {
                                return cmp >= 0 ? 1L : -1L;
                            } else {
                                return cmp >= 0 ? -1L : 1L;
                            }
                        })
                ));
    }

    @Transactional
    public void deletePost(Long id, Long userByTokenId) {
        Post post = postRepository.getReferenceById(id);
        if (LocalDateTime.now().isBefore(post.getCreatedAt().plusDays(7))) {
            throw new IllegalStateException("帖子需发布满 7 天后方可删除");
        }
        if (post.getUserId()-userByTokenId == 0) {
            postCommentRepository.deleteAllByPostId(id);
            postRepository.deleteById(id);
        }
    }
}