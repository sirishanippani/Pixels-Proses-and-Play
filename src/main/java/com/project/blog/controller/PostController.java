package com.project.blog.controller;

import com.project.blog.dto.CommentForm;
import com.project.blog.dto.PostForm;
import com.project.blog.exception.PostOwnershipException;
import com.project.blog.model.Post;
import com.project.blog.security.AppUserDetails;
import com.project.blog.service.CommentService;
import com.project.blog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/post")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    public PostController(PostService postService, CommentService commentService) {
        this.postService = postService;
        this.commentService = commentService;
    }

    @GetMapping("/create")
    public String createPostForm(Model model) {
        model.addAttribute("postForm", PostForm.empty());
        return "create_post";
    }

    @PostMapping("/create")
    public String savePost(@Valid @ModelAttribute("postForm") PostForm form,
                           BindingResult bindingResult,
                           @AuthenticationPrincipal AppUserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "create_post";
        }
        postService.createPost(userDetails.getUser(), form.title(), form.content(), form.tagInput());
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
        model.addAttribute("postForm", PostForm.from(post));
        model.addAttribute("postId", id);
        return "edit_post";
    }

    @GetMapping("/{id}")
    public String viewPost(@PathVariable Long id, Model model,
                           @AuthenticationPrincipal AppUserDetails userDetails) {
        Post post = postService.getPostForView(id);
        model.addAttribute("post", post);
        model.addAttribute("comments", commentService.getCommentsForPost(post));

        // Only seed an empty comment form if the comment controller didn't flash one back
        // (it flashes commentForm + its BindingResult on validation failure).
        if (!model.containsAttribute("commentForm")) {
            model.addAttribute("commentForm", CommentForm.empty());
        }

        if (userDetails != null) {
            model.addAttribute("currentUser", userDetails.getUser());
        }
        model.addAttribute("loggedInUser", userDetails != null ? userDetails.getUser() : null);

        return "post_detail";
    }

    @PostMapping("/edit/{id}")
    public String updatePost(@PathVariable Long id,
                             @Valid @ModelAttribute("postForm") PostForm form,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal AppUserDetails userDetails,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", id);
            return "edit_post";
        }
        try {
            postService.updatePost(id, userDetails.getUser(),
                    form.title(), form.content(), form.tagInput());
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
