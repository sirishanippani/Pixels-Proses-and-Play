package com.project.blog.controller;

import com.project.blog.model.Comment;
import com.project.blog.model.Post;
import com.project.blog.repository.CommentRepository;
import com.project.blog.repository.PostRepository;
import com.project.blog.security.AppUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @PostMapping("/post/{postId}/comment")
    public String addComment(@PathVariable Long postId,
                             @ModelAttribute("newComment") Comment newComment,
                             @AuthenticationPrincipal AppUserDetails loggedInUser,
                             RedirectAttributes redirectAttributes) {

        Post post = postRepository.findById(postId).orElseThrow();
        newComment.setPost(post);
        newComment.setUser(loggedInUser.getUser());
        newComment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(newComment);
        redirectAttributes.addFlashAttribute("flashMessage", "💬 Comment added!");
        return "redirect:/post/" + postId;
    }

    @PostMapping("/comment/{id}/delete")
    public String deleteComment(@PathVariable Long id,
                                @AuthenticationPrincipal AppUserDetails loggedInUser,
                                RedirectAttributes redirectAttributes) {
        Comment comment = commentRepository.findById(id).orElseThrow();

        if (!comment.getUser().getId().equals(loggedInUser.getUser().getId())) {
            redirectAttributes.addFlashAttribute("flashMessage", "🛑 Not your comment to delete!");
            return "redirect:/post/" + comment.getPost().getId();
        }

        commentRepository.delete(comment);
        redirectAttributes.addFlashAttribute("flashMessage", "🧹 Comment erased from existence.");
        return "redirect:/post/" + comment.getPost().getId();
    }

}
