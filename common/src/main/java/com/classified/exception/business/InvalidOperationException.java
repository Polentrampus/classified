package com.classified.exception.business;

import com.classified.exception.ErrorCode;

public class InvalidOperationException extends BusinessException {
    public InvalidOperationException(String message) {
        super(ErrorCode.INVALID_OPERATION, message);
    }
}
