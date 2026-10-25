package com.project.blog.dto;

import com.project.blog.model.Post;
import com.project.blog.model.Tag;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostSummaryMapper {

    private static final int SUMMARY_LENGTH = 150;

    public PostSummaryDto toDto(Post post) {
        return new PostSummaryDto(
                post.getId(),
                post.getTitle(),
                buildSummary(post.getContent()),
                post.getUser() != null ? post.getUser().getUsername() : null,
                post.getCreatedAt(),
                extractTagNames(post)
        );
    }

    private String buildSummary(String content) {
        if (content == null) return "";
        String plain = content.replaceAll("<[^>]*>", "");
        return plain.length() > SUMMARY_LENGTH
                ? plain.substring(0, SUMMARY_LENGTH) + "..."
                : plain;
    }

    private List<String> extractTagNames(Post post) {
        if (post.getTags() == null) return List.of();
        return post.getTags().stream()
                .map(Tag::getName)
                .sorted()
                .toList();
    }
}