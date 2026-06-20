package at.fhtw.tourplanner.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Custom exception for business logic errors with detailed error information.
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final List<String> details;

    public BusinessException(String message, HttpStatus httpStatus) {
        this(message, httpStatus, null, null);
    }

    public BusinessException(String message, HttpStatus httpStatus, String errorCode) {
        this(message, httpStatus, errorCode, null);
    }

    public BusinessException(String message, HttpStatus httpStatus, List<String> details) {
        this(message, httpStatus, null, details);
    }

    public BusinessException(String message, HttpStatus httpStatus, String errorCode, List<String> details) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.details = details;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public List<String> getDetails() {
        return details;
    }
}
