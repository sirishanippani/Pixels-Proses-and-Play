package com.project.blog.service;

import com.project.blog.model.Post;
import com.project.blog.model.Role;
import com.project.blog.model.User;
import com.project.blog.repository.PostRepository;
import com.project.blog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public AdminService(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Transactional
    public boolean toggleUserRole(Long userId, String requesterEmail) {
        return userRepository.findById(userId).map(user -> {
            Role newRole = "ROLE_USER".equals(user.getRole()) ? Role.ROLE_ADMIN : Role.ROLE_USER;
            user.setRole(newRole);
            userRepository.save(user);
            return user.getEmail().equals(requesterEmail);
        }).orElse(false);
    }
}
