package com.zingly.events.common.exceptionHandler;

import java.time.Instant;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/** Carries a consistent error response returned by REST APIs. */
public class ApiErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String code;
    private String message;
    private String path;
    private Map<String, String> fieldErrors;
}
