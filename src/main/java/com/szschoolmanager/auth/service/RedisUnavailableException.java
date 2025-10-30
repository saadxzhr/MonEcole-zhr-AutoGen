package com.szschoolmanager.auth.service;

public class RedisUnavailableException extends RuntimeException {
    public RedisUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
    public RedisUnavailableException(String message) {
        super(message);
    }
}
