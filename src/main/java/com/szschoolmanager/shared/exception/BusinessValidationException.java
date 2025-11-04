package com.szschoolmanager.shared.exception;

public class BusinessValidationException extends RuntimeException {
  public BusinessValidationException(String message) {
    super(message);
  }
}
