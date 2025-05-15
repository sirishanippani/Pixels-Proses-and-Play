package com.project.blog.controller;

import com.project.blog.model.Post;
import com.project.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SearchController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/search")
    public List<String> searchPosts(@RequestParam("q") String query) {
        System.out.println("Searching for: " + query);
        return postRepository.findByTitleContainingIgnoreCase(query)
                .stream()
                .map(Post::getTitle)
                .collect(Collectors.toList());
    }
}
