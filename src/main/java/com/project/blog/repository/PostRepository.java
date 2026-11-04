package com.project.blog.repository;

import com.project.blog.model.Post;
import com.project.blog.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "tags"})
    Optional<Post> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"user", "tags"})
    List<Post> findAll();

    @EntityGraph(attributePaths = {"user", "tags"})
    List<Post> findByUser(User user);

    List<Post> findByTitleContainingIgnoreCase(String keyword);

    @EntityGraph(attributePaths = {"user", "tags"})
    List<Post> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT p.id FROM Post p")
    List<Long> findAllIds();

    @Override
    @EntityGraph(attributePaths = {"user", "tags"})
    List<Post> findAllById(Iterable<Long> ids);
}
