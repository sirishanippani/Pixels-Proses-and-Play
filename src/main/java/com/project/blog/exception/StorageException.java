package com.project.blog.exception;

public class StorageException extends RuntimeException {

    private final String messageCode;

    public StorageException(String messageCode, String message) {
        super(message);
        this.messageCode = messageCode;
    }

    public StorageException(String messageCode, String message, Throwable cause) {
        super(message, cause);
        this.messageCode = messageCode;
    }

    public String getMessageCode() {
        return messageCode;
    }
}
