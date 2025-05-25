package com.project.blog.controller;

import com.project.blog.model.Post;
import com.project.blog.model.User;
import com.project.blog.repository.PostRepository;
import com.project.blog.repository.UserRepository;
import com.project.blog.security.AppUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Value("${profile.upload.dir}")
    private String uploadDir;

    @GetMapping("/{username}")
    public String viewProfile(@PathVariable String username, Model model, @AuthenticationPrincipal AppUserDetails currentUser) {
        User user = userRepository.findByUsername(username);
        if (user == null) return "redirect:/";

        List<Post> posts = postRepository.findByUser(user);

        boolean isOwner = currentUser != null && currentUser.getUsername().equals(user.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("posts", posts);
        model.addAttribute("isOwner", isOwner);

        return "profile";
    }

    @GetMapping("/edit")
    public String editProfileForm(@AuthenticationPrincipal AppUserDetails currentUser, Model model) {
        User user = userRepository.findByUsername(currentUser.getUsername());
        model.addAttribute("user", user);

        return "edit_profile";
    }

    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute("user") User formUser, @AuthenticationPrincipal AppUserDetails currentUser, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(currentUser.getUsername());
        user.setUsername(formUser.getUsername());
        user.setBio(formUser.getBio());

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "\uD83D\uDC85 Your glow-up is now official!");

        return "redirect:/profile/" + user.getUsername();
    }

    @PostMapping("/upload")
    public String uploadProfileImage(@RequestParam("image") MultipartFile file,
                                     @AuthenticationPrincipal AppUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("flashMessage", "\uD83D\uDCC2 You forgot the file, darling.");
                return "redirect:/profile";
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir, filename);
            Files.copy(file.getInputStream(), path);

            User user = userDetails.getUser();
            user.setProfileImage(filename);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("flashMessage", "\uD83E\uDE9E New look, who dis?");
            return "redirect:/profile/" + userDetails.getUsername();
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("flashMessage", "\uD83D\uDEAB Oops, that file wasn’t giving what it needed to give.");
            return "redirect:/profile/" + userDetails.getUsername();
        }
    }
}
