package com.classified.exception.technical;

import com.classified.exception.ClassifiedException;
import com.classified.exception.ErrorCode;

public class TechnicalException extends ClassifiedException {
    public TechnicalException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    public TechnicalException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
