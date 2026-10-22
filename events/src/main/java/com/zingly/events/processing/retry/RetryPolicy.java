package com.zingly.events.processing.retry;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import com.zingly.events.config.ProcessingProperties;
import org.springframework.stereotype.Component;

@Component
/** Calculates retry eligibility and exponential backoff delays. */
public class RetryPolicy {

    private final ProcessingProperties.Retry properties;

    /** Creates a retry policy with its required state. */
    public RetryPolicy(ProcessingProperties properties) {
        this.properties = properties.retry();
    }

    /** Checks whether the current failure exhausts the retry budget. */
    public boolean shouldDeadLetter(int failureNumber) {
        return failureNumber >= properties.maxFailures();
    }

    /** Calculates capped exponential backoff with positive jitter. */
    public Duration delayFor(int failureNumber) {
        double exponential = properties.initialDelay().toMillis()
                * Math.pow(properties.multiplier(), Math.max(0, failureNumber - 1));
        double jitter = exponential * properties.jitterRatio() * ThreadLocalRandom.current().nextDouble();
        long delayed = Math.round(exponential + jitter);
        return Duration.ofMillis(Math.min(delayed, properties.maxDelay().toMillis()));
    }
}
