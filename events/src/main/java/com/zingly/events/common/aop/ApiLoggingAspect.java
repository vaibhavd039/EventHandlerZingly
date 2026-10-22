package com.zingly.events.common.aop;

import static com.zingly.events.common.constants.LoggingConstants.*;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.stream.Collectors;

/** Logs the input, output, status, and duration of every REST controller invocation. */
@Aspect
@Component
@Slf4j
public class ApiLoggingAspect {

    private static final int MAX_LOG_VALUE_LENGTH = 4_000;

    /** Wraps every REST API invocation with request, response, and exception logging. */
    @Around(REST_CONTROLLER_POINTCUT)
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        String handler = joinPoint.getSignature().toShortString();
        RequestDetails request = currentRequest();
        String input = formatArguments(joinPoint.getArgs());

        log.info(API_REQUEST_TEMPLATE,
                request.method(), request.uri(), handler, input);

        try {
            Object output = joinPoint.proceed();
            long durationMs = elapsedMilliseconds(startedAt);
            log.info(API_RESPONSE_TEMPLATE,
                    request.method(), request.uri(), handler, responseStatus(output), durationMs, safeValue(output));
            return output;
        } catch (Throwable exception) {
            long durationMs = elapsedMilliseconds(startedAt);
            log.error(API_FAILURE_TEMPLATE,
                    request.method(), request.uri(), handler, durationMs,
                    exception.getClass().getSimpleName(), safeValue(exception.getMessage()));
            throw exception;
        }
    }

    /** Reads request metadata when the intercepted method is running inside an HTTP request. */
    private RequestDetails currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return new RequestDetails(attributes.getRequest().getMethod(), attributes.getRequest().getRequestURI());
        }
        return new RequestDetails(UNKNOWN_VALUE, UNKNOWN_VALUE);
    }

    /** Converts controller arguments into one bounded log value while excluding servlet infrastructure objects. */
    private String formatArguments(Object[] arguments) {
        return Arrays.stream(arguments)
                .filter(argument -> !(argument instanceof ServletRequest))
                .filter(argument -> !(argument instanceof ServletResponse))
                .map(this::safeValue)
                .collect(Collectors.joining(ARGUMENT_SEPARATOR, LIST_PREFIX, LIST_SUFFIX));
    }

    /** Returns the HTTP status carried by a response or the default successful status. */
    private int responseStatus(Object output) {
        if (output instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getStatusCode().value();
        }
        return 200;
    }

    /** Converts a value to bounded single-line text so large payloads cannot flood the logs. */
    private String safeValue(Object value) {
        String text = String.valueOf(value).replace('\n', ' ').replace('\r', ' ');
        if (text.length() <= MAX_LOG_VALUE_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_LOG_VALUE_LENGTH) + TRUNCATED_SUFFIX;
    }

    /** Calculates elapsed execution time in milliseconds. */
    private long elapsedMilliseconds(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    /** Holds the HTTP method and URI associated with one intercepted controller call. */
    private record RequestDetails(String method, String uri) {
    }
}
