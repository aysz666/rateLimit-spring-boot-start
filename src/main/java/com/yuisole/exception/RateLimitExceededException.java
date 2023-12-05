package com.yuisole.exception;

/**
 * @author Yuisole
 */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}