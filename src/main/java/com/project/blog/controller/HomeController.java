package com.project.blog.controller;

import com.project.blog.dto.PostSummaryDto;
import com.project.blog.dto.PostSummaryMapper;
import com.project.blog.model.Post;
import com.project.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final PostRepository postRepository;
    private final PostSummaryMapper summaryMapper;

    public HomeController(PostRepository postRepository, PostSummaryMapper summaryMapper) {
        this.postRepository = postRepository;
        this.summaryMapper = summaryMapper;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<PostSummaryDto> featured = postRepository.findRandomPosts().stream()
                .map(summaryMapper::toDto).toList();
        model.addAttribute("featuredPosts", featured);

        List<PostSummaryDto> recent = postRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(summaryMapper::toDto).toList();
        model.addAttribute("recentPosts", recent);

        return "index";
    }


}