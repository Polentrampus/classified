package classified.exception.business;

import classified.exception.ClassifiedException;
import classified.exception.ErrorCode;

/// Ошибка, которая может возникать в бизнес-логике приложения (4хх)
public class BusinessException extends ClassifiedException {
    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
