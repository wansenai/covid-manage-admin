package com.summer.common.esearch;

public class EsearchException extends RuntimeException {
    public EsearchException(String message) {
        super(message);
    }

    public EsearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
