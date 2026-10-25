package com.project.blog.controller;

import com.project.blog.dto.PostSummaryDto;
import com.project.blog.dto.PostSummaryMapper;
import com.project.blog.model.Post;
import com.project.blog.model.User;
import com.project.blog.repository.PostRepository;
import com.project.blog.repository.UserRepository;
import com.project.blog.security.AppUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final PostRepository postRepository;
    private final PostSummaryMapper summaryMapper;

    public DashboardController(PostRepository postRepository, PostSummaryMapper summaryMapper) {
        this.postRepository = postRepository;
        this.summaryMapper = summaryMapper;
    }

    @GetMapping("/dashboard")
    public String userDashboard(Model model, @AuthenticationPrincipal AppUserDetails userDetails) {
        List<PostSummaryDto> posts = postRepository.findByUser(userDetails.getUser()).stream()
                .map(summaryMapper::toDto).toList();
        model.addAttribute("posts", posts);
        return "dashboard";
    }
}