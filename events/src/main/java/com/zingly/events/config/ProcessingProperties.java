package com.zingly.events.config;

import static com.zingly.events.common.constants.ConfigurationConstants.*;
import static com.zingly.events.common.constants.ErrorConstants.*;
import com.zingly.events.common.exception.ApplicationValidationException;
import java.time.Duration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = PROCESSING_PROPERTIES_PREFIX)
/** Binds and validates event-processing settings from application configuration. */
public record ProcessingProperties(
        @Min(1) int workerCount,
        @Min(1) int readyQueueCapacity,
        @Min(1) int schedulerThreads,
        @NotNull @Valid Backlog backlog,
        @NotNull @Valid Retry retry,
        @NotNull @Valid Restore restore) {

    /** Holds the maximum unresolved-event capacity setting. */
    public record Backlog(@Min(1) int maxUnresolvedEvents) {
    }

    /** Holds retry limits, delay, jitter, and failure-simulation settings. */
    public record Retry(
            @Min(1) int maxFailures,
            @NotNull Duration initialDelay,
            @DecimalMin(DECIMAL_ONE) double multiplier,
            @NotNull Duration maxDelay,
            @DecimalMin(DECIMAL_ZERO) @DecimalMax(DECIMAL_ONE) double jitterRatio,
            @DecimalMin(DECIMAL_ZERO) @DecimalMax(DECIMAL_ONE) double transientFailureRate) {

        /** Creates a retry with its required state. */
        public Retry {
            requirePositive(initialDelay, INITIAL_DELAY_FIELD);
            requirePositive(maxDelay, MAX_DELAY_FIELD);
            if (initialDelay.compareTo(maxDelay) > 0) {
                throw new ApplicationValidationException(INITIAL_DELAY_EXCEEDS_MAX);
            }
        }
    }

    /** Holds startup restore and reconciliation timing settings. */
    public record Restore(
            @NotNull Duration reconciliationInterval,
            @NotNull Duration staleRunningThreshold) {

        /** Creates a restore with its required state. */
        public Restore {
            requirePositive(reconciliationInterval, RECONCILIATION_INTERVAL_FIELD);
            requirePositive(staleRunningThreshold, STALE_RUNNING_THRESHOLD_FIELD);
        }
    }

    /** Rejects a duration that is zero, negative, or missing. */
    private static void requirePositive(Duration value, String fieldName) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new ApplicationValidationException(fieldName + MUST_BE_POSITIVE_SUFFIX);
        }
    }
}
