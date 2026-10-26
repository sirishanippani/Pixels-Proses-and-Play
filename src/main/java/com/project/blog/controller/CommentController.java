package com.project.blog.controller;

import com.project.blog.exception.CommentOwnershipException;
import com.project.blog.model.Comment;
import com.project.blog.security.AppUserDetails;
import com.project.blog.service.CommentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/post/{postId}/comment")
    public String addComment(@PathVariable Long postId,
                             @ModelAttribute("newComment") Comment newComment,
                             @AuthenticationPrincipal AppUserDetails loggedInUser,
                             RedirectAttributes redirectAttributes) {
        commentService.createComment(postId, loggedInUser.getUser(), newComment.getContent());
        redirectAttributes.addFlashAttribute("flashMessage", "💬 Comment added!");
        return "redirect:/post/" + postId;
    }

    @PostMapping("/comment/{id}/delete")
    public String deleteComment(@PathVariable Long id,
                                @AuthenticationPrincipal AppUserDetails loggedInUser,
                                RedirectAttributes redirectAttributes) {
        try {
            Long postId = commentService.deleteComment(id, loggedInUser.getUser());
            redirectAttributes.addFlashAttribute("flashMessage", "🧹 Comment erased from existence.");
            return "redirect:/post/" + postId;
        } catch (CommentOwnershipException e) {
            redirectAttributes.addFlashAttribute("flashMessage", "🛑 Not your comment to delete!");
            return "redirect:/post/" + e.getPostId();
        }
    }
}
