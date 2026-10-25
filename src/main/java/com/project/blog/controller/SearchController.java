package com.project.blog.controller;

import com.project.blog.dto.PostSearchResultDto;
import com.project.blog.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    private final PostService postService;

    public SearchController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/search")
    public List<PostSearchResultDto> searchPosts(@RequestParam("q") String query) {
        log.debug("Searching for: {}", query);
        return postService.searchByTitle(query);
    }
}
