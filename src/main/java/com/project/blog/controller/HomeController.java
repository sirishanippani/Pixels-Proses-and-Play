package com.project.blog.controller;

import com.project.blog.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final PostService postService;

    public HomeController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredPosts", postService.getFeaturedPosts());
        model.addAttribute("recentPosts", postService.getRecentPosts());
        return "index";
    }
}
