package at.fhtw.tourplanner.controller;

import at.fhtw.tourplanner.util.ApiResponseUtil;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseUtil.ApiResponse<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(error -> error.getField()))
                .map(error -> {
                    String field = error.getField();
                    String errorMessage = error.getDefaultMessage();
                    fieldErrors.put(field, errorMessage);
                    return field + ": " + errorMessage;
                })
                .collect(Collectors.toList());

        String message = "Validation failed";
        if (!details.isEmpty()) {
            message = "Validation failed: " + String.join("; ", details);
        }

        return ApiResponseUtil.error(message, HttpStatus.BAD_REQUEST, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseUtil.ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        List<String> details = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String field = violation.getPropertyPath().toString();
                    String errorMessage = violation.getMessage();
                    fieldErrors.put(field, errorMessage);
                    return field + ": " + errorMessage;
                })
                .collect(Collectors.toList());

        String message = "Validation failed";
        if (!details.isEmpty()) {
            message = "Validation failed: " + String.join("; ", details);
        }

        return ApiResponseUtil.error(message, HttpStatus.BAD_REQUEST, fieldErrors);
    }
}
