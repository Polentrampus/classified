package com.classified;

import com.classified.dto.ErrorResponse;
import com.classified.exception.ClassifiedException;
import com.classified.exception.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.transaction.TransactionSystemException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Наши бизнес-исключения, наследуемые от ClassifiedException
    @ExceptionHandler(ClassifiedException.class)
    public ResponseEntity<ErrorResponse> handleClassifiedException (ClassifiedException ex){
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("Business exception: code={}, message={}", errorCode.getCode(), ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.getHttpStatus().getReasonPhrase())
                .code(errorCode.getCode())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.badRequest().body(response);
    }

    // Ошибки валидации @Valid (MethodArgumentNotValidException)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidate(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ErrorCode.VALIDATION_ERROR.getDefaultMessage())
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // Ошибки валидации на уровне JPA/Hibernate
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // Доступ запрещён (Spring Security)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ErrorCode.ACCESS_DENIED.getHttpStatus().value())
                .error(ErrorCode.ACCESS_DENIED.getHttpStatus().getReasonPhrase())
                .code(ErrorCode.ACCESS_DENIED.getCode())
                .message(ErrorCode.ACCESS_DENIED.getDefaultMessage())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // Все остальные необработанные исключения
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unhandled exception: ", ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .code(ErrorCode.INTERNAL_ERROR.getCode())
                .message(ErrorCode.INTERNAL_ERROR.getDefaultMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ErrorResponse> handleTransactionSystemException(TransactionSystemException ex) {
        // Извлекаем ConstraintViolationException из причины
        Throwable cause = ex.getRootCause();

        if (cause instanceof ConstraintViolationException validationEx) {
            Map<String, String> errors = new HashMap<>();
            for (ConstraintViolation<?> violation : validationEx.getConstraintViolations()) {
                String field = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                errors.put(field, message);
            }

            ErrorResponse response = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .code(ErrorCode.VALIDATION_ERROR.getCode())
                    .message(ErrorCode.VALIDATION_ERROR.getDefaultMessage())
                    .validationErrors(errors)
                    .build();

            return ResponseEntity.badRequest().body(response);
        }

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .code(ErrorCode.INTERNAL_ERROR.getCode())
                .message(ErrorCode.INTERNAL_ERROR.getDefaultMessage())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
