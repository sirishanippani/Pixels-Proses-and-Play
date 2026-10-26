package com.project.blog.dto;

import com.project.blog.model.Post;
import com.project.blog.model.Tag;
import com.project.blog.validation.NotBlankHtml;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.stream.Collectors;

public record PostForm(

        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @NotBlankHtml(message = "Content is required")
        @Size(max = 50_000, message = "Content is too long")
        String content,

        @Size(max = 500, message = "Tags input is too long")
        String tagInput

) {
    public static PostForm empty() {
        return new PostForm(null, null, null);
    }

    public static PostForm from(Post post) {
        String tagCsv = post.getTags().stream()
                .map(Tag::getName)
                .sorted()
                .collect(Collectors.joining(", "));
        return new PostForm(post.getTitle(), post.getContent(), tagCsv);
    }
}
