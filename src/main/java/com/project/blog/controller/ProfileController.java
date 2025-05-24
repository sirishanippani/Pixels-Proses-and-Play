package com.project.blog.controller;

import com.project.blog.model.Post;
import com.project.blog.model.User;
import com.project.blog.repository.PostRepository;
import com.project.blog.repository.UserRepository;
import com.project.blog.security.AppUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

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
        redirectAttributes.addFlashAttribute("success", "Profile updated!");

        return "redirect:/profile/" + user.getUsername();
    }
}
