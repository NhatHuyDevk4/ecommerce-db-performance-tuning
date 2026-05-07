package com.example.ecommerce_performance_tuning.exception;

import com.example.ecommerce_performance_tuning.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {
        log.warn("Not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiResponse.<Void>builder()
                .status(404)
                .message(ex.getMessage())
                .path(req.getRequestURI())
                .error(ApiResponse.ErrorDetail.builder()
                    .code(ex.getErrorCode())
                    .message(ex.getMessage())
                    .detail(Map.of(
                        "resource", ex.getResourceName() != null ? ex.getResourceName() : "unknown",
                        "field", ex.getFieldName() != null ? ex.getFieldName() : "unknown",
                        "value", ex.getFieldValue() != null ? ex.getFieldValue() : "unknown"
                    ))
                    .build())
                .build()
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            BadRequestException ex, HttpServletRequest req) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse.<Void>builder()
                .status(400)
                .message(ex.getMessage())
                .path(req.getRequestURI())
                .error(ApiResponse.ErrorDetail.builder()
                    .code(ex.getErrorCode())
                    .message(ex.getMessage())
                    .build())
                .build()
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedException ex, HttpServletRequest req) {
        log.warn("Unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ApiResponse.<Void>builder()
                .status(401)
                .message(ex.getMessage())
                .path(req.getRequestURI())
                .error(ApiResponse.ErrorDetail.builder()
                    .code(ex.getErrorCode())
                    .message("Authentication required")
                    .detail(ex.getMessage())
                    .build())
                .build()
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(
            ForbiddenException ex, HttpServletRequest req) {
        log.warn("Forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ApiResponse.<Void>builder()
                .status(403)
                .message(ex.getMessage())
                .path(req.getRequestURI())
                .error(ApiResponse.ErrorDetail.builder()
                    .code(ex.getErrorCode())
                    .message(ex.getMessage())
                    .build())
                .build()
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest req) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ApiResponse.<Void>builder()
                .status(409)
                .message(ex.getMessage())
                .path(req.getRequestURI())
                .error(ApiResponse.ErrorDetail.builder()
                    .code(ex.getErrorCode())
                    .message(ex.getMessage())
                    .detail(Map.of(
                        "resource", ex.getResourceName() != null ? ex.getResourceName() : "unknown",
                        "field", ex.getFieldName() != null ? ex.getFieldName() : "unknown",
                        "value", ex.getFieldValue() != null ? ex.getFieldValue() : "unknown"
                    ))
                    .build())
                .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fieldError -> fieldError.getDefaultMessage() != null
                    ? fieldError.getDefaultMessage()
                    : "Invalid value",
                (existing, replacement) -> existing
            ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse.<Void>builder()
                .status(400)
                .message("Validation failed")
                .path(req.getRequestURI())
                .error(ApiResponse.ErrorDetail.builder()
                    .code("VALIDATION_ERROR")
                    .message("Input validation failed")
                    .detail(fieldErrors)
                    .build())
                .build()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        log.warn("Type mismatch: {}", ex.getMessage());
        String detail = String.format(
            "Parameter '%s' should be of type '%s', but got '%s'",
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
            ex.getValue()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse.<Void>builder()
                .status(400)
                .message("Invalid parameter type")
                .path(req.getRequestURI())
                .error(ApiResponse.ErrorDetail.builder()
                    .code("TYPE_MISMATCH")
                    .message("Parameter type mismatch")
                    .detail(detail)
                    .build())
                .build()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest req) {
        log.warn("Missing parameter: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse.<Void>builder()
                .status(400)
                .message("Missing required parameter: " + ex.getParameterName())
                .path(req.getRequestURI())
                .error(ApiResponse.ErrorDetail.builder()
                    .code("MISSING_PARAMETER")
                    .message("Required parameter '" + ex.getParameterName() + "' is missing")
                    .detail(Map.of("parameter", ex.getParameterName()))
                    .build())
                .build()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.warn("Message not readable: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ApiResponse.<Void>builder()
                .status(400)
                .message("Invalid request body")
                .path(req.getRequestURI())
                .error(ApiResponse.ErrorDetail.builder()
                    .code("MESSAGE_NOT_READABLE")
                    .message("Request body is missing or malformed")
                    .detail(ex.getMostSpecificCause().getMessage())
                    .build())
                .build()
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiResponse.<Void>builder()
                .status(404)
                .message("Endpoint not found")
                .path(req.getRequestURI())
                .error(ApiResponse.ErrorDetail.builder()
                    .code("ENDPOINT_NOT_FOUND")
                    .message("The requested endpoint does not exist")
                    .detail(ex.getMessage())
                    .build())
                .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest req) {
        log.error("Unexpected error at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse.<Void>builder()
                .status(500)
                .message("An unexpected error occurred")
                .path(req.getRequestURI())
                .error(ApiResponse.ErrorDetail.builder()
                    .code("INTERNAL_ERROR")
                    .message(ex.getClass().getSimpleName())
                    .detail(ex.getMessage())
                    .build())
                .build()
        );
    }
}
