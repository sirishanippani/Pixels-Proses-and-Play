package com.project.blog.dto;

public class PostSearchResultDto {
    private Long id;
    private String title;

    public PostSearchResultDto(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
