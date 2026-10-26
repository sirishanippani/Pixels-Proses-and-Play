package com.project.blog.exception;

public class CommentOwnershipException extends RuntimeException {

    private final Long postId;

    public CommentOwnershipException(String message, Long postId) {
        super(message);
        this.postId = postId;
    }

    public Long getPostId() {
        return postId;
    }
}
