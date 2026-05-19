package com.department.identityservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse {
    private String message;
    private HttpStatus status;
    private Object data;
    private String error;

    private static ApiResponse success(String message, HttpStatus status, Object data)
    {
        return new ApiResponse(message, status, data, null);
    }

    private static  ApiResponse error(String message, HttpStatus status, String error)
    {
        return new ApiResponse(message, status, null, error);
    }
}
