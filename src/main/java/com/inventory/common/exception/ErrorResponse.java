package com.inventory.common.exception;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String error;
    private LocalDateTime timestamp;
    private Map<String, String> validationErrors;

    public ErrorResponse(int status, String error) {
        this.status = status;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, Map<String, String> validationErrors) {
        this.status = status;
        this.error = error;
        this.timestamp = LocalDateTime.now();
        this.validationErrors = validationErrors;
    }
}
