package com.sameneed.exception;

public class AuthException extends RuntimeException {

    private final int httpStatus;

    public AuthException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
