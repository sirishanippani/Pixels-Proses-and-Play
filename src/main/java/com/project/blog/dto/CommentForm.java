package com.project.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentForm(

        @NotBlank(message = "Comment can't be empty")
        @Size(max = 2000, message = "Comment must be at most 2000 characters")
        String content

) {
    public static CommentForm empty() {
        return new CommentForm(null);
    }
}
