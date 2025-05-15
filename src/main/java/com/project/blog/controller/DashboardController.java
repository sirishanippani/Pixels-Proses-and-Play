package com.project.blog.controller;

import com.project.blog.model.Post;
import com.project.blog.model.User;
import com.project.blog.repository.PostRepository;
import com.project.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        model.addAttribute("posts", postRepository.findByUser(user));
        return "dashboard";
    }

    @GetMapping("/post/create")
    public String createPostForm(Model model) {
        model.addAttribute("post", new Post());
        return "create_post";
    }

    @PostMapping("/post/create")
    public String savePost(@ModelAttribute("post") Post post, Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        post.setUser(user);
        postRepository.save(post);
        return "redirect:/dashboard";
    }
}