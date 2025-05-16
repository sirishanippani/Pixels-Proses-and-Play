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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String savePost(@ModelAttribute("post") Post post, Authentication auth, RedirectAttributes redirectAttributes) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email);
        post.setUser(user);
        postRepository.save(post);
        redirectAttributes.addFlashAttribute("flashMessage", "Post created successfully!");
        return "redirect:/dashboard";
    }

    @GetMapping("/post/edit/{id}")
    public String editPostForm(@PathVariable Long id, Model model, Authentication auth) {
        Post post = postRepository.findById(id).orElseThrow();
        if (!post.getUser().getEmail().equals(auth.getName())) {
            return "redirect:/dashboard";
        }
        model.addAttribute("post", post);
        return "edit_post";
    }

    @PostMapping("/post/edit/{id}")
    public String updatePost(@PathVariable Long id, @ModelAttribute Post updatedPost, Authentication auth) {
        Post post = postRepository.findById(id).orElseThrow();
        if (post.getUser().getEmail().equals(auth.getName())) {
            post.setTitle(updatedPost.getTitle());
            post.setContent(updatedPost.getContent());
            postRepository.save(post);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/post/delete/{id}")
    public String deletePost(@PathVariable Long id, Authentication auth){
        Post post = postRepository.findById(id).orElseThrow();
        if (post.getUser().getEmail().equals(auth.getName())){
            postRepository.delete(post);
        }
        return "redirect:/dashboard";
    }
}