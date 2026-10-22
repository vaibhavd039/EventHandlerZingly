package com.zingly.events.common.exceptionHandler;

import static com.zingly.events.common.constants.ErrorConstants.*;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.zingly.events.common.exception.BacklogFullException;
import com.zingly.events.common.exception.ConflictException;
import com.zingly.events.common.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
/** Translates application and validation exceptions into HTTP error responses. */
public class GlobalExceptionHandler {

    private final Clock clock;

    /** Creates a global exception handler with its required state. */
    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    /** Returns field-level details for request validation failures. */
    ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return response(
                HttpStatus.BAD_REQUEST,
                VALIDATION_FAILED_CODE,
                VALIDATION_FAILED_MESSAGE,
                request,
                fieldErrors);
    }

    @ExceptionHandler(ConflictException.class)
    /** Returns an HTTP conflict response. */
    ResponseEntity<ApiErrorResponse> handleConflict(
            ConflictException exception,
            HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, CONFLICT_CODE, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    /** Returns an HTTP not-found response. */
    ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, NOT_FOUND_CODE, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(BacklogFullException.class)
    /** Returns an HTTP rate-limit response when the backlog is full. */
    ResponseEntity<ApiErrorResponse> handleBacklogFull(
            BacklogFullException exception,
            HttpServletRequest request) {
        return response(HttpStatus.TOO_MANY_REQUESTS, BACKLOG_FULL_CODE, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    /** Returns an HTTP bad-request response. */
    ResponseEntity<ApiErrorResponse> handleBadRequest(
            IllegalArgumentException exception,
            HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, BAD_REQUEST_CODE, exception.getMessage(), request, Map.of());
    }

    /** Builds the shared REST error response body. */
    private ResponseEntity<ApiErrorResponse> response(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors) {
        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(Instant.now(clock))
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(code)
                .message(message)
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
