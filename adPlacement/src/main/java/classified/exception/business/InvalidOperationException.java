package classified.exception.business;

import classified.exception.ErrorCode;

public class InvalidOperationException extends BusinessException {
    public InvalidOperationException(String message) {
        super(ErrorCode.INVALID_OPERATION, message);
    }
}
