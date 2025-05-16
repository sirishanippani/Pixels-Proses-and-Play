package com.project.blog.controller;

import com.project.blog.model.Post;
import com.project.blog.model.User;
import com.project.blog.repository.PostRepository;
import com.project.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminDashboard(Model model, Authentication auth) {
        System.out.println("Logged in user: " + auth.getName());
        auth.getAuthorities().forEach(role -> System.out.println("Role: " + role));
        List<User> users = userRepository.findAll();
        List<Post> posts = postRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("posts", posts);
        return "admin_dashboard";
    }

    @GetMapping("/whoami")
    public String whoami(org.springframework.security.core.Authentication auth) {
        System.out.println("\n=== WHOAMI DEBUG ===");
        System.out.println("Logged-in user: " + auth.getName());
        auth.getAuthorities().forEach(role ->
                System.out.println("Authority: " + role.getAuthority())
        );
        System.out.println("====================\n");
        return "redirect:/";
    }
}
