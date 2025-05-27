package com.project.blog.controller;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/dashboard")
    public String userDashboard(Model model, @AuthenticationPrincipal AppUserDetails userDetails, HttpServletRequest request) {
        System.out.println("CSRF token in request: " + request.getAttribute("_csrf"));
        User user = userDetails.getUser(); // or getId()

        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("_csrf", csrfToken);

        List<Post> posts = postRepository.findByUser(user);
        posts.forEach(p -> {
            String plainText = p.getContent().replaceAll("<[^>]*>", "");
            if (plainText.length() > 150) {
                plainText = plainText.substring(0, 150) + "...";
            }
            p.setContent(plainText);
        });

        model.addAttribute("posts", posts);
        return "dashboard";
    }
}