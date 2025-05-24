package com.project.blog.controller;

import com.project.blog.model.Post;
import com.project.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/")
    public String home(Model model) {
        List<Post> recentPosts = postRepository.findTop10ByOrderByCreatedAtDesc();
        model.addAttribute("recentPosts", recentPosts);
        return "index";
    }


}