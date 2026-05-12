package com.classified.exception;

public abstract class ClassifiedException extends RuntimeException {
    private final ErrorCode errorCode;

    protected ClassifiedException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected ClassifiedException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
