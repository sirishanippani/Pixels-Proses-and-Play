package com.project.blog.controller;

import com.project.blog.dto.ProfileForm;
import com.project.blog.exception.StorageException;
import com.project.blog.model.User;
import com.project.blog.security.AppUserDetails;
import com.project.blog.service.PostService;
import com.project.blog.service.StorageService;
import com.project.blog.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final PostService postService;
    private final StorageService storageService;

    public ProfileController(UserService userService, PostService postService, StorageService storageService) {
        this.userService = userService;
        this.postService = postService;
        this.storageService = storageService;
    }

    @GetMapping("/{username}")
    public String viewProfile(@PathVariable String username, Model model, @AuthenticationPrincipal AppUserDetails currentUser) {
        return userService.findByUsername(username).map(user -> {
            boolean isOwner = currentUser != null && currentUser.getUsername().equals(user.getUsername());
            model.addAttribute("user", user);
            model.addAttribute("posts", postService.getPostsByUser(user));
            model.addAttribute("isOwner", isOwner);
            return "profile";
        }).orElse("redirect:/");
    }

    @GetMapping("/edit")
    public String editProfileForm(@AuthenticationPrincipal AppUserDetails currentUser, Model model) {
        User user = userService.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user missing from database: " + currentUser.getUsername()));
        model.addAttribute("profileForm", ProfileForm.from(user));
        model.addAttribute("currentUsername", user.getUsername());
        return "edit_profile";
    }

    @PostMapping("/edit")
    public String updateProfile(@Valid @ModelAttribute("profileForm") ProfileForm form,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal AppUserDetails currentUser,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUsername", currentUser.getUsername());
            return "edit_profile";
        }
        User updated = userService.updateProfile(currentUser.getUsername(), form.username(), form.bio());
        redirectAttributes.addFlashAttribute("success", "💅 Your glow-up is now official!");
        return "redirect:/profile/" + updated.getUsername();
    }

    @PostMapping("/upload")
    public String uploadProfileImage(@RequestParam("image") MultipartFile file,
                                     @AuthenticationPrincipal AppUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        try {
            String storedName = storageService.store(file);
            userService.setProfileImage(userDetails.getUser().getId(), storedName);
            redirectAttributes.addFlashAttribute("flashMessage", "🪞 New look, who dis?");
        } catch (StorageException e) {
            redirectAttributes.addFlashAttribute("flashMessage", "🚫 " + e.getMessage());
        }
        return "redirect:/profile/" + userDetails.getUsername();
    }
}
