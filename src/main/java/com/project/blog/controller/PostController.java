package com.project.blog.controller;

import com.project.blog.model.Comment;
import com.project.blog.model.Post;
import com.project.blog.repository.CommentRepository;
import com.project.blog.repository.PostRepository;
import com.project.blog.security.AppUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/create")
    public String createPostForm(Model model) {
        model.addAttribute("post", new Post());
        return "create_post";
    }

    @PostMapping("/create")
    public String savePost(@ModelAttribute("post") Post post,
                           @AuthenticationPrincipal AppUserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        post.setUser(userDetails.getUser());
        postRepository.save(post);
        redirectAttributes.addFlashAttribute("flashMessage", "✨ Your thoughts are now out in the wild!");
        return "redirect:/dashboard";
    }

    @GetMapping("/edit/{id}")
    public String editPostForm(@PathVariable Long id,
                               Model model,
                               @AuthenticationPrincipal AppUserDetails userDetails) {
        Post post = postRepository.findById(id).orElseThrow();
        if (!post.getUser().getUsername().equals(userDetails.getUsername())) {
            return "redirect:/dashboard";
        }
        model.addAttribute("post", post);
        return "edit_post";
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model,
                           @AuthenticationPrincipal AppUserDetails userDetails) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post ID: " + id));
        model.addAttribute("post", post);

        List<Comment> comments = commentRepository.findByPost(post);

        if (userDetails != null) {
            model.addAttribute("currentUser", userDetails.getUser());
        }

        model.addAttribute("comments", comments);
        model.addAttribute("newComment", new Comment());
        model.addAttribute("loggedInUser", userDetails != null ? userDetails.getUser() : null);

        return "post_detail";
    }

    @PostMapping("/edit/{id}")
    public String updatePost(@PathVariable Long id,
                             @ModelAttribute Post updatedPost,
                             @AuthenticationPrincipal AppUserDetails userDetails) {
        Post post = postRepository.findById(id).orElseThrow();
        if (post.getUser().getUsername().equals(userDetails.getUsername())) {
            post.setTitle(updatedPost.getTitle());
            post.setContent(updatedPost.getContent());
            postRepository.save(post);
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id,
                             @AuthenticationPrincipal AppUserDetails userDetails) {
        Post post = postRepository.findById(id).orElseThrow();
        if (post.getUser().getUsername().equals(userDetails.getUsername())) {
            postRepository.delete(post);
        }
        return "redirect:/dashboard";
    }
}