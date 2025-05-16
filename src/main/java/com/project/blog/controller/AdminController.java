package com.project.blog.controller;

import com.project.blog.model.Post;
import com.project.blog.model.Role;
import com.project.blog.model.User;
import com.project.blog.repository.PostRepository;
import com.project.blog.repository.UserRepository;
import com.project.blog.security.AppUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminDashboard(Model model, Authentication auth) {
        AppUserDetails userDetails = (AppUserDetails) auth.getPrincipal();
        User user = userDetails.getUser();
        List<User> users = userRepository.findAll();
        List<Post> posts = postRepository.findAll();
        model.addAttribute("admin", user.getUsername());
        model.addAttribute("users", users);
        model.addAttribute("posts", posts);
        model.addAttribute("flashMessage", model.asMap().get("flashMessage"));
        return "admin_dashboard";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/user/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("flashMessage", "User deleted successfully!");
        return "redirect:/admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/post/delete/{id}")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        postRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("flashMessage", "Post deleted successfully!");
        return "redirect:/admin";
    }

    @PostMapping("/admin/user/toggle-role/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String toggleUserRole(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication auth) {
        userRepository.findById(id).ifPresent(user -> {
            boolean isSelf = user.getEmail().equals(auth.getName());
            if (user.getRole().equals("ROLE_USER")) {
                user.setRole(Role.ROLE_ADMIN);
            } else {
                user.setRole(Role.ROLE_USER);
            }
            userRepository.save(user);
            if (isSelf) {
                redirectAttributes.addFlashAttribute("flashMessage", "Your role has changed. Please log in again.");
                // redirect to logout so user session is refreshed
                redirectAttributes.addFlashAttribute("forceLogout", true);
            }
        });
        redirectAttributes.addFlashAttribute("flashMessage", "User role toggled successfully!");
        return "redirect:/admin";
    }

    @GetMapping("/whoami")
    public String whoami(org.springframework.security.core.Authentication auth) {
        System.out.println("\n=== WHOAMI DEBUG ===");
        System.out.println("Logged-in user: " + auth.getName());
        auth.getAuthorities().forEach(role ->
                System.out.println("Authority: " + role.getAuthority())
        );
        System.out.println("====================\n");
        return "redirect:/";
    }
}
