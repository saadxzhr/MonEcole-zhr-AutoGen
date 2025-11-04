package com.szschoolmanager.auth.exception;

public class RedisUnavailableException extends RuntimeException {
  public RedisUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }

  public RedisUnavailableException(String message) {
    super(message);
  }
}
