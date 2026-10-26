package com.project.blog.service;

import com.project.blog.exception.UserNotFoundException;
import com.project.blog.model.User;
import com.project.blog.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findOptionalByUsername(username);
    }

    @Transactional
    public User register(String username, String email, String rawPassword) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        // role intentionally not accepted here; defaults to ROLE_USER via the entity field initializer
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(String currentUsername, String newUsername, String newBio) {
        User user = userRepository.findOptionalByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException(currentUsername));
        user.setUsername(newUsername);
        user.setBio(newBio);
        return userRepository.save(user);
    }

    @Transactional
    public void setProfileImage(Long userId, String filename) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(userId)));
        user.setProfileImage(filename);
        userRepository.save(user);
    }
}
