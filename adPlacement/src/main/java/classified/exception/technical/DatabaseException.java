package classified.exception.technical;

import classified.exception.ErrorCode;

public class DatabaseException extends TechnicalException {
    public DatabaseException(Object identifier, Throwable throwable) {
        super(ErrorCode.DATABASE_ERROR,
                String.format("Exception classified.entity %s", identifier), throwable);
    }
}
