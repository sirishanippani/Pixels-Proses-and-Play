package com.project.blog.dto;

import com.project.blog.model.Post;
import org.springframework.stereotype.Component;

@Component
public class PostSearchResultMapper {

    public PostSearchResultDto toDto(Post post) {
        return new PostSearchResultDto(post.getId(), post.getTitle());
    }
}
