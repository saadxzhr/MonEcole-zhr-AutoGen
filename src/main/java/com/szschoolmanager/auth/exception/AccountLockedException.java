package com.szschoolmanager.auth.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception levée lorsqu'un compte est temporairement verrouillé.
 */
public class AccountLockedException extends AuthenticationException {
    public AccountLockedException(String msg) {
        super(msg);
    }
}
