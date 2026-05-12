package com.classified.exception.technical;

import com.classified.exception.ErrorCode;

public class DatabaseException extends TechnicalException {
    public DatabaseException(Object identifier, Throwable throwable) {
        super(ErrorCode.DATABASE_ERROR,
                String.format("Exception entity %s", identifier), throwable);
    }
}
