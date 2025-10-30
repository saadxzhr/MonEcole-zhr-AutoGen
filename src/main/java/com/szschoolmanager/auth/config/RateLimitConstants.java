package com.szschoolmanager.auth.config;

import java.time.Duration;

public final class RateLimitConstants {
    
    private RateLimitConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // Rate limiting configuration
    public static final int LOGIN_LIMIT = 5;
    public static final Duration LOGIN_WINDOW = Duration.ofMinutes(1);
    
    public static final int API_LIMIT = 100;
    public static final Duration API_WINDOW = Duration.ofMinutes(1);
    
    public static final int REFRESH_LIMIT = 10;
    public static final Duration REFRESH_WINDOW = Duration.ofMinutes(5);
    
    // Redis key prefixes
    public static final String RATE_LIMIT_PREFIX = "rl:";
    public static final String BLACKLIST_PREFIX = "blacklist:access:";
    
    // Max token length
    public static final int MAX_TOKEN_LENGTH = 8192;
    
    // IP extraction
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String X_REAL_IP = "X-Real-IP";
}