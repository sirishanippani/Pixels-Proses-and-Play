package com.project.blog.controller;

import com.project.blog.dto.CommentForm;
import com.project.blog.exception.CommentOwnershipException;
import com.project.blog.flash.FlashService;
import com.project.blog.security.AppUserDetails;
import com.project.blog.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CommentController {

    private static final String COMMENT_FORM_ATTR = "commentForm";
    private static final String COMMENT_FORM_BINDING_RESULT_ATTR =
            "org.springframework.validation.BindingResult." + COMMENT_FORM_ATTR;

    private final CommentService commentService;
    private final FlashService flash;

    public CommentController(CommentService commentService, FlashService flash) {
        this.commentService = commentService;
        this.flash = flash;
    }

    @PostMapping("/post/{postId}/comment")
    public String addComment(@PathVariable Long postId,
                             @Valid @ModelAttribute("commentForm") CommentForm form,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal AppUserDetails loggedInUser,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(COMMENT_FORM_BINDING_RESULT_ATTR, bindingResult);
            redirectAttributes.addFlashAttribute(COMMENT_FORM_ATTR, form);
            return "redirect:/post/" + postId;
        }
        commentService.createComment(postId, loggedInUser.getUser(), form.content());
        flash.success(redirectAttributes, "comment.posted");
        return "redirect:/post/" + postId;
    }

    @PostMapping("/comment/{id}/delete")
    public String deleteComment(@PathVariable Long id,
                                @AuthenticationPrincipal AppUserDetails loggedInUser,
                                RedirectAttributes redirectAttributes) {
        try {
            Long postId = commentService.deleteComment(id, loggedInUser.getUser());
            flash.success(redirectAttributes, "comment.deleted");
            return "redirect:/post/" + postId;
        } catch (CommentOwnershipException e) {
            flash.error(redirectAttributes, "comment.ownership.denied");
            return "redirect:/post/" + e.getPostId();
        }
    }
}
