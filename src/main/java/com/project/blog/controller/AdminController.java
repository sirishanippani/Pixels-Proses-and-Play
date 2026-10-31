package com.project.blog.controller;

import com.project.blog.security.AppUserDetails;
import com.project.blog.service.AdminService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminDashboard(Model model, Authentication auth) {
        AppUserDetails userDetails = (AppUserDetails) auth.getPrincipal();
        model.addAttribute("admin", userDetails.getUser().getUsername());
        model.addAttribute("users", adminService.getAllUsers());
        model.addAttribute("posts", adminService.getAllPosts());
        model.addAttribute("flashMessage", model.asMap().get("flashMessage"));
        return "admin_dashboard";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/user/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.deleteUser(id);
        redirectAttributes.addFlashAttribute("flashMessage", "💀 One less drama queen in the cast.");
        return "redirect:/admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/post/delete/{id}")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.deletePost(id);
        redirectAttributes.addFlashAttribute("flashMessage", "🔥 You just Thanos-snapped that post.");
        return "redirect:/admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/user/toggle-role/{id}")
    public String toggleUserRole(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication auth) {
        boolean affectedSelf = adminService.toggleUserRole(id, auth.getName());
        if (affectedSelf) {
            redirectAttributes.addFlashAttribute("flashMessage", "👑 New role unlocked! Time to re-enter like royalty.");
            redirectAttributes.addFlashAttribute("forceLogout", true);
        } else {
            redirectAttributes.addFlashAttribute("flashMessage", "🔁 Someone just got a promotion... or demotion. Tea! ☕");
        }
        return "redirect:/admin";
    }
}
