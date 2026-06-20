package at.fhtw.tourplanner.exception;

import at.fhtw.tourplanner.util.ApiResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponseUtil.ApiResponse<Object>> handleResponseStatusException(ResponseStatusException ex) {
        log.warn("ResponseStatusException: {} - {}", ex.getStatusCode(), ex.getReason());
        return ApiResponseUtil.error(ex.getReason(), HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponseUtil.ApiResponse<Object>> handleBusinessException(BusinessException ex) {
        log.warn("BusinessException: {} - {}", ex.getErrorCode(), ex.getMessage());
        if (ex.getDetails() != null && !ex.getDetails().isEmpty()) {
            return ApiResponseUtil.error(ex.getMessage(), ex.getHttpStatus(), ex.getDetails());
        }
        return ApiResponseUtil.error(ex.getMessage(), ex.getHttpStatus());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseUtil.ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("ResourceNotFoundException: {} with id '{}'", ex.getResourceType(), ex.getResourceId());
        String message = String.format("%s with id '%s' not found", ex.getResourceType(), ex.getResourceId());
        return ApiResponseUtil.error(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseUtil.ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return ApiResponseUtil.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponseUtil.ApiResponse<Object>> handleSecurityException(SecurityException ex) {
        log.warn("SecurityException: {}", ex.getMessage());
        return ApiResponseUtil.error("Access denied: " + ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseUtil.ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ApiResponseUtil.error("An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
