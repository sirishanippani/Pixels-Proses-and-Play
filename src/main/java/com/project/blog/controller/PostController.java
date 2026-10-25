package com.project.blog.controller;

import com.project.blog.exception.PostOwnershipException;
import com.project.blog.model.Comment;
import com.project.blog.model.Post;
import com.project.blog.model.Tag;
import com.project.blog.repository.CommentRepository;
import com.project.blog.security.AppUserDetails;
import com.project.blog.service.PostService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/post")
public class PostController {

    private final PostService postService;
    private final CommentRepository commentRepository;

    public PostController(PostService postService, CommentRepository commentRepository) {
        this.postService = postService;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/create")
    public String createPostForm(Model model) {
        model.addAttribute("post", new Post());
        return "create_post";
    }

    @PostMapping("/create")
    public String savePost(@ModelAttribute("post") Post post,
                           @RequestParam("tagInput") String tagInput,
                           @AuthenticationPrincipal AppUserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        postService.createPost(userDetails.getUser(), post.getTitle(), post.getContent(), tagInput);
        redirectAttributes.addFlashAttribute("flashMessage", "✨ Your thoughts are now out in the wild!");
        return "redirect:/dashboard";
    }

    @GetMapping("/edit/{id}")
    public String editPostForm(@PathVariable Long id,
                               Model model,
                               @AuthenticationPrincipal AppUserDetails userDetails) {
        Post post;
        try {
            post = postService.getPostForEdit(id, userDetails.getUser());
        } catch (PostOwnershipException e) {
            return "redirect:/dashboard";
        }
        model.addAttribute("post", post);

        String tagString = post.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));
        model.addAttribute("postTagString", tagString);

        return "edit_post";
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model,
                           @AuthenticationPrincipal AppUserDetails userDetails) {
        Post post = postService.getPostForView(id);
        model.addAttribute("post", post);

        List<Comment> comments = commentRepository.findByPost(post);
        model.addAttribute("comments", comments);
        model.addAttribute("newComment", new Comment());

        if (userDetails != null) {
            model.addAttribute("currentUser", userDetails.getUser());
        }
        model.addAttribute("loggedInUser", userDetails != null ? userDetails.getUser() : null);

        return "post_detail";
    }

    @PostMapping("/edit/{id}")
    public String updatePost(@PathVariable Long id,
                             @ModelAttribute Post updatedPost,
                             @RequestParam("tagInput") String tagInput,
                             @AuthenticationPrincipal AppUserDetails userDetails) {
        try {
            postService.updatePost(id, userDetails.getUser(),
                    updatedPost.getTitle(), updatedPost.getContent(), tagInput);
        } catch (PostOwnershipException e) {
            return "redirect:/dashboard";
        }
        return "redirect:/post/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id,
                             @AuthenticationPrincipal AppUserDetails userDetails) {
        try {
            postService.deletePost(id, userDetails.getUser());
        } catch (PostOwnershipException e) {
            // silently ignore — preserves existing behavior; UX bug flagged for later
        }
        return "redirect:/dashboard";
    }
}
