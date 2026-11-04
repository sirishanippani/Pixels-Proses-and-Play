package com.project.blog.controller;

import com.project.blog.flash.FlashService;
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
    private final FlashService flash;

    public AdminController(AdminService adminService, FlashService flash) {
        this.adminService = adminService;
        this.flash = flash;
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
        flash.success(redirectAttributes, "admin.user.deleted");
        return "redirect:/admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/post/delete/{id}")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.deletePost(id);
        flash.success(redirectAttributes, "admin.post.deleted");
        return "redirect:/admin";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/user/toggle-role/{id}")
    public String toggleUserRole(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication auth) {
        boolean affectedSelf = adminService.toggleUserRole(id, auth.getName());
        if (affectedSelf) {
            flash.success(redirectAttributes, "admin.user.role.toggled.self");
            redirectAttributes.addFlashAttribute("forceLogout", true);
        } else {
            flash.success(redirectAttributes, "admin.user.role.toggled");
        }
        return "redirect:/admin";
    }
}
