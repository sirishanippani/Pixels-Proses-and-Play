package com.project.blog.controller;

import com.project.blog.model.User;
import com.project.blog.security.AppUserDetails;
import com.project.blog.service.PostService;
import com.project.blog.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final PostService postService;

    @Value("${profile.upload.dir}")
    private String uploadDir;

    public ProfileController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
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
        model.addAttribute("user", user);
        return "edit_profile";
    }

    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute("user") User formUser,
                                @AuthenticationPrincipal AppUserDetails currentUser,
                                RedirectAttributes redirectAttributes) {
        User updated = userService.updateProfile(currentUser.getUsername(), formUser.getUsername(), formUser.getBio());
        redirectAttributes.addFlashAttribute("success", "💅 Your glow-up is now official!");
        return "redirect:/profile/" + updated.getUsername();
    }

    @PostMapping("/upload")
    public String uploadProfileImage(@RequestParam("image") MultipartFile file,
                                     @AuthenticationPrincipal AppUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("flashMessage", "📂 You forgot the file, darling.");
                return "redirect:/profile";
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir, filename);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path);

            userService.setProfileImage(userDetails.getUser().getId(), filename);

            redirectAttributes.addFlashAttribute("flashMessage", "🪞 New look, who dis?");
            return "redirect:/profile/" + userDetails.getUsername();
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("flashMessage", "🚫 Oops, that file wasn’t giving what it needed to give.");
            return "redirect:/profile/" + userDetails.getUsername();
        }
    }
}
