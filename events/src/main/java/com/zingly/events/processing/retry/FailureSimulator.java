package com.zingly.events.processing.retry;

import static com.zingly.events.common.constants.ErrorConstants.*;
import static com.zingly.events.common.constants.ProcessingConstants.*;
import com.zingly.events.common.exception.SimulatedTransientFailureException;
import com.zingly.events.config.ProcessingProperties;
import com.zingly.events.event.persistence.domain.EventInboxEntity;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
/** Produces deterministic transient and requested persistent failures for testing. */
public class FailureSimulator {

    private final double transientFailureRate;

    /** Creates a failure simulator with its required state. */
    public FailureSimulator(ProcessingProperties properties) {
        this.transientFailureRate = properties.retry().transientFailureRate();
    }

    /** Simulates configured transient or payload-requested failures. */
    public void maybeFail(EventInboxEntity event, JsonNode payload) {
        JsonNode requestedFailure = payload.get(SIMULATE_FAILURE_FIELD);
        if (requestedFailure != null
                && requestedFailure.isTextual()
                && ALWAYS_FAILURE.equalsIgnoreCase(requestedFailure.stringValue())) {
            throw new SimulatedTransientFailureException(PERSISTENT_FAILURE_REQUESTED);
        }
        int bucket = Math.floorMod(event.getEventId().hashCode(), 10_000);
        if (event.getAttemptStarts() == 1 && bucket < Math.round(transientFailureRate * 10_000)) {
            throw new SimulatedTransientFailureException(TRANSIENT_FAILURE_SIMULATED);
        }
    }
}
