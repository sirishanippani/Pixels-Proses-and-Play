package com.project.blog.service;

import com.project.blog.dto.PostSummaryDto;
import com.project.blog.dto.PostSummaryMapper;
import com.project.blog.model.User;
import com.project.blog.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostSummaryMapper summaryMapper;

    public PostService(PostRepository postRepository, PostSummaryMapper summaryMapper) {
        this.postRepository = postRepository;
        this.summaryMapper = summaryMapper;
    }

    public List<PostSummaryDto> getFeaturedPosts() {
        return postRepository.findRandomPosts().stream()
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
}
