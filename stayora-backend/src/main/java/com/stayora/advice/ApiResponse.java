package com.stayora.advice;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ApiResponse<T> {

    private LocalDateTime timestamp = LocalDateTime.now();
    private T data;
    private ApiError apiError;

    public ApiResponse(T data) {
        this.data = data;
    }

    public ApiResponse(ApiError apiError) {
        this.apiError = apiError;
    }
}