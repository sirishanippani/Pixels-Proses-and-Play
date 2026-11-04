package com.project.blog.repository;

import com.project.blog.model.Comment;
import com.project.blog.model.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = "user")
    List<Comment> findByPost(Post post);
}
