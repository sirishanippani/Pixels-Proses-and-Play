package com.project.blog.repository;

import com.project.blog.model.Post;
import com.project.blog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUser(User user);
    List<Post> findByTitleContainingIgnoreCase(String keyword);
    List<Post> findTop10ByOrderByCreatedAtDesc();

    @Query(value = "SELECT * FROM posts ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<Post> findRandomPosts();

}
