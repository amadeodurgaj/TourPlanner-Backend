package at.fhtw.tourplanner.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class ApiResponseUtil {

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, data, null, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(false, message, null, status.value(), LocalDateTime.now()));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(String message, HttpStatus status, List<String> details) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(false, message, null, status.value(), LocalDateTime.now(), details));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(String message, HttpStatus status, Map<String, String> fieldErrors) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(false, message, null, status.value(), LocalDateTime.now(), null, fieldErrors));
    }

    public record ApiResponse<T>(
            boolean success,
            String message,
            T data,
            Integer errorCode,
            LocalDateTime timestamp,
            List<String> details,
            Map<String, String> fieldErrors
    ) {
        public ApiResponse(boolean success, String message, T data, Integer errorCode, LocalDateTime timestamp) {
            this(success, message, data, errorCode, timestamp, null, null);
        }

        public ApiResponse(boolean success, String message, T data, Integer errorCode, LocalDateTime timestamp, List<String> details) {
            this(success, message, data, errorCode, timestamp, details, null);
        }
    }
}