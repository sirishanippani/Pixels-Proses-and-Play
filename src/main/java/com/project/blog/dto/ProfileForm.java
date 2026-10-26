package com.project.blog.dto;

import com.project.blog.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileForm(

        @NotBlank(message = "Display name is required")
        @Size(min = 3, max = 32, message = "Display name must be 3-32 characters")
        String username,

        @Size(max = 500, message = "Bio must be at most 500 characters")
        String bio

) {
    public static ProfileForm from(User user) {
        return new ProfileForm(user.getUsername(), user.getBio());
    }
}
