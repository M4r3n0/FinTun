package com.tunfin.payment.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<java.util.Map<String, String>> handleException(Exception ex) {
        String message = ex.getMessage();
        if (ex.getCause() != null) {
            message += " | Caused by: " + ex.getCause().getMessage();
        }
        return ResponseEntity.status(500).body(java.util.Map.of(
                "message", message,
                "exception", ex.getClass().getSimpleName()));
    }
}
