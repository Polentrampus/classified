package classified.exception.technical;

import classified.exception.ClassifiedException;
import classified.exception.ErrorCode;

public class TechnicalException extends ClassifiedException {
    protected TechnicalException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
