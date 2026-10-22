package com.zingly.events.common.constants;

/** Defines AOP and worker logging templates and formatting tokens. */
public interface LoggingConstants {

    String REST_CONTROLLER_POINTCUT = "within(@org.springframework.web.bind.annotation.RestController *)";
    String API_REQUEST_TEMPLATE = "API request method={} uri={} handler={} input={}";
    String API_RESPONSE_TEMPLATE = "API response method={} uri={} handler={} status={} durationMs={} output={}";
    String API_FAILURE_TEMPLATE = "API failure method={} uri={} handler={} durationMs={} errorType={} message={}";
    String UNKNOWN_VALUE = "UNKNOWN";
    String ARGUMENT_SEPARATOR = ", ";
    String LIST_PREFIX = "[";
    String LIST_SUFFIX = "]";
    String TRUNCATED_SUFFIX = "...<truncated>";
    String UNCAUGHT_EXCEPTION_PREFIX = "Uncaught exception in ";
    String UNEXPECTED_WORKER_FAILURE = "Unexpected worker-loop failure";
}
