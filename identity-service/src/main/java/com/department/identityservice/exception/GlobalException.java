package com.department.identityservice.exception;

import com.department.identityservice.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.hc.client5.http.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request
    ) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error(
                        ex.getMessage(),
                        HttpStatus.UNAUTHORIZED,
                        "UNAUTHORIZED"
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request
    ) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error(
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "INTERNAL_SERVER_ERROR"
                )
        );
    }
}
