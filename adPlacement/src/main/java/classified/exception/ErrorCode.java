package classified.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 4xx Client Errors
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid request content"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Requested resource does not exist"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", "Resource already exists"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Insufficient permissions to perform this action"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Full authentication is required"),
    INVALID_OPERATION(HttpStatus.BAD_REQUEST, "INVALID_OPERATION", "Operation cannot be performed"),
    INSUFFICIENT_QUANTITY(HttpStatus.BAD_REQUEST, "INSUFFICIENT_QUANTITY", "Requested quantity is not available"),
    INCORRECT_SETTINGS_SORT(HttpStatus.BAD_REQUEST,"INCORRECT_SETTINGS_SORT","Incorrect sorting settings"),
    FAILED_TO_MAP_ENTITY(HttpStatus.BAD_REQUEST,"FAILED_TO_MAP_ENTITY", "failed to map classified.entity"),

    // 5xx Server Errors
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected internal error occurred"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_ERROR", "Database operation failed"),
    EXTERNAL_SERVICE_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_SERVICE_ERROR", "External classified.service is unavailable");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}