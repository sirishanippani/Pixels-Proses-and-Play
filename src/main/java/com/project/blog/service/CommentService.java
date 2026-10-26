package com.project.blog.service;

import com.project.blog.exception.CommentNotFoundException;
import com.project.blog.exception.CommentOwnershipException;
import com.project.blog.model.Comment;
import com.project.blog.model.Post;
import com.project.blog.model.User;
import com.project.blog.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;

    public CommentService(CommentRepository commentRepository, PostService postService) {
        this.commentRepository = commentRepository;
        this.postService = postService;
    }

    public List<Comment> getCommentsForPost(Post post) {
        return commentRepository.findByPost(post);
    }

    @Transactional
    public Comment createComment(Long postId, User author, String content) {
        Post post = postService.getPostForView(postId);
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(author);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Transactional
    public Long deleteComment(Long id, User requester) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));
        Long parentPostId = comment.getPost().getId();
        if (!comment.getUser().getId().equals(requester.getId())) {
            throw new CommentOwnershipException(
                    "User " + requester.getUsername() + " does not own comment " + id,
                    parentPostId);
        }
        commentRepository.delete(comment);
        return parentPostId;
    }
}
