package com.project.blog.controller;

import com.project.blog.security.AppUserDetails;
import com.project.blog.service.PostService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final PostService postService;

    public DashboardController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/dashboard")
    public String userDashboard(Model model, @AuthenticationPrincipal AppUserDetails userDetails) {
        model.addAttribute("posts", postService.getPostsByUser(userDetails.getUser()));
        return "dashboard";
    }
}
