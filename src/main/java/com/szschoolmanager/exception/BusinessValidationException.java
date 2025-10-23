package com.szschoolmanager.exception;

public class BusinessValidationException extends RuntimeException {
  public BusinessValidationException(String message) {
    super(message);
  }
}
