package com.classified.exception.business;

import com.classified.exception.ErrorCode;
import com.classified.exception.ClassifiedException;

/// Ошибка, которая может возникать в бизнес-логике приложения (4хх)
public class BusinessException extends ClassifiedException {
    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
