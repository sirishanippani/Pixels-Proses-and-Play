package com.project.blog.service;

import com.project.blog.dto.PostSearchResultDto;
import com.project.blog.dto.PostSearchResultMapper;
import com.project.blog.dto.PostSummaryDto;
import com.project.blog.dto.PostSummaryMapper;
import com.project.blog.exception.PostNotFoundException;
import com.project.blog.exception.PostOwnershipException;
import com.project.blog.model.Post;
import com.project.blog.model.User;
import com.project.blog.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostSummaryMapper summaryMapper;
    private final PostSearchResultMapper searchResultMapper;
    private final TagService tagService;

    public PostService(PostRepository postRepository,
                       PostSummaryMapper summaryMapper,
                       PostSearchResultMapper searchResultMapper,
                       TagService tagService) {
        this.postRepository = postRepository;
        this.summaryMapper = summaryMapper;
        this.searchResultMapper = searchResultMapper;
        this.tagService = tagService;
    }

    private static final int FEATURED_POST_COUNT = 3;

    public List<PostSummaryDto> getFeaturedPosts() {
        List<Long> allIds = new ArrayList<>(postRepository.findAllIds());
        Collections.shuffle(allIds);
        List<Long> picked = allIds.stream().limit(FEATURED_POST_COUNT).toList();
        return postRepository.findAllById(picked).stream()
                .map(summaryMapper::toDto)
                .toList();
    }

    public List<PostSummaryDto> getRecentPosts() {
        return postRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(summaryMapper::toDto)
                .toList();
    }

    public List<PostSummaryDto> getPostsByUser(User user) {
        return postRepository.findByUser(user).stream()
                .map(summaryMapper::toDto)
                .toList();
    }

    public List<PostSearchResultDto> searchByTitle(String query) {
        return postRepository.findByTitleContainingIgnoreCase(query).stream()
                .map(searchResultMapper::toDto)
                .toList();
    }

    public Post getPostForView(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    public Post getPostForEdit(Long id, User requester) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        assertOwnership(post, requester);
        return post;
    }

    @Transactional
    public Post createPost(User author, String title, String content, String tagCsv) {
        Post post = new Post();
        post.setUser(author);
        post.setTitle(title);
        post.setContent(content);
        post.setTags(tagService.resolveOrCreate(tagCsv));
        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(Long id, User requester, String title, String content, String tagCsv) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        assertOwnership(post, requester);
        post.setTitle(title);
        post.setContent(content);
        post.setTags(tagService.resolveOrCreate(tagCsv));
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id, User requester) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        assertOwnership(post, requester);
        postRepository.delete(post);
    }

    private void assertOwnership(Post post, User requester) {
        if (!post.getUser().getUsername().equals(requester.getUsername())) {
            throw new PostOwnershipException(
                    "User " + requester.getUsername() + " does not own post " + post.getId());
        }
    }
}
