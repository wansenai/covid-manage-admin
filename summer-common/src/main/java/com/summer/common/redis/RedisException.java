package com.summer.common.redis;

public class RedisException extends RuntimeException {
    public RedisException(String message) {
        super(message);
    }

    @SuppressWarnings("unused")
    public RedisException(String message, Throwable cause) {
        super(message, cause);
    }
}
