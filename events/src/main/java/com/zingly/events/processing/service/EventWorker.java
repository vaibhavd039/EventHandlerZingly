package com.zingly.events.processing.service;

import static com.zingly.events.common.constants.DatabaseConstants.*;
import static com.zingly.events.common.constants.ErrorConstants.*;
import static com.zingly.events.common.constants.ProcessingConstants.*;
import com.zingly.events.common.exception.ApplicationValidationException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.zingly.events.config.ProcessingProperties;
import com.zingly.events.event.domain.enums.ProcessingOutcome;
import com.zingly.events.event.persistence.domain.EventInboxEntity;
import com.zingly.events.customer_preference.service.dto.PreferenceDecision;
import com.zingly.events.customer_preference.service.UserPreferenceService;
import com.zingly.events.notification_service_provider.NotificationServiceProvider;
import com.zingly.events.notification_service_provider.factory.NotificationServiceFactory;
import com.zingly.events.processing.coordinator.BacklogTracker;
import com.zingly.events.processing.coordinator.CustomerKey;
import com.zingly.events.processing.coordinator.ProcessingDecision;
import com.zingly.events.processing.retry.FailureSimulator;
import com.zingly.events.tenant.persistence.domain.TenantConfigEntity;
import com.zingly.events.tenant.service.TenantConfigService;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
/** Validates, enriches, and persists the next eligible event for a customer. */
public class EventWorker {

    private final EventProcessingStore processingStore;
    private final TenantConfigService tenantConfigService;
    private final UserPreferenceService preferenceService;
    private final NotificationServiceFactory handlerFactory;
    private final FailureSimulator failureSimulator;
    private final BacklogTracker backlogTracker;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Duration uncertainStateDelay;

    /** Creates an event worker with its required state. */
    public EventWorker(
            EventProcessingStore processingStore,
            TenantConfigService tenantConfigService,
            UserPreferenceService preferenceService,
            NotificationServiceFactory handlerFactory,
            FailureSimulator failureSimulator,
            BacklogTracker backlogTracker,
            ObjectMapper objectMapper,
            Clock clock,
            ProcessingProperties properties) {
        this.processingStore = processingStore;
        this.tenantConfigService = tenantConfigService;
        this.preferenceService = preferenceService;
        this.handlerFactory = handlerFactory;
        this.failureSimulator = failureSimulator;
        this.backlogTracker = backlogTracker;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.uncertainStateDelay = properties.restore().staleRunningThreshold();
    }

    /** Claims and processes the next eligible event for one customer. */
    public ProcessingDecision processNext(CustomerKey key) {
        ClaimResult claim = processingStore.claimHead(key, Instant.now(clock));
        return switch (claim.getType()) {
            case NOTHING -> ProcessingDecision.nothing();
            case BLOCKED -> ProcessingDecision.blocked();
            case WAIT -> ProcessingDecision.waitFor(claim.getDelay());
            case CLAIMED -> processClaimed(claim.getEvent());
        };
    }

    /** Prepares and commits one already-claimed event attempt. */
    private ProcessingDecision processClaimed(EventInboxEntity event) {
        ProcessingCandidate candidate;
        try {
            candidate = prepareResult(event);
        } catch (Exception processingFailure) {
            return recordProcessingFailure(event, processingFailure);
        }

        try {
            processingStore.complete(
                    event,
                    candidate.outcome,
                    candidate.enrichedPayload,
                    candidate.tenantConfigVersion,
                    candidate.preferenceVersion,
                    Instant.now(clock));
            backlogTracker.release();
            return ProcessingDecision.advance();
        } catch (RuntimeException uncertainCommit) {
            System.getLogger(getClass().getName()).log(
                    System.Logger.Level.ERROR,
                    RESULT_COMMIT_UNCONFIRMED_PREFIX + event.getEventId(),
                    uncertainCommit);
            return ProcessingDecision.waitFor(uncertainStateDelay);
        }
    }

    /** Validates, enriches, and builds the candidate processing result. */
    private ProcessingCandidate prepareResult(EventInboxEntity event) throws Exception {
        JsonNode payload = objectMapper.readTree(event.getPayload());
        failureSimulator.maybeFail(event, payload);

        TenantConfigEntity tenantConfig = tenantConfigService.getRequired(
                event.getTenantId(), event.getChannel());
        if (!tenantConfig.isEnabled()) {
            throw new ApplicationValidationException(event.getChannel() + CHANNEL_DISABLED_INFIX + event.getTenantId());
        }

        NotificationServiceProvider handler = handlerFactory.getHandler(event.getChannel());
        handler.validate(payload);
        PreferenceDecision preference = preferenceService.resolve(
                event.getTenantId(),
                event.getCustomerId(),
                event.getNotificationType(),
                event.getChannel(),
                tenantConfig);
        ProcessingOutcome outcome = preference.isEnabled()
                ? ProcessingOutcome.DELIVERED
                : ProcessingOutcome.SUPPRESSED_BY_PREFERENCE;

        Map<String, Object> enriched = new LinkedHashMap<>();
        enriched.put(PAYLOAD_COLUMN, payload);
        enriched.put(ENRICHED_TENANT_ID_FIELD, event.getTenantId());
        enriched.put(ENRICHED_TENANT_CONFIG_VERSION_FIELD, tenantConfig.getConfigVersion());
        enriched.put(ENRICHED_HANDLER_FIELD, handler.handlerName());
        enriched.put(ENRICHED_NOTIFICATION_ENABLED_FIELD, preference.isEnabled());
        enriched.put(ENRICHED_PREFERENCE_VERSION_FIELD, preference.getVersion());
        enriched.put(OUTCOME_COLUMN, outcome.name());
        return new ProcessingCandidate(
                outcome,
                objectMapper.writeValueAsString(enriched),
                tenantConfig.getConfigVersion(),
                preference.getVersion());
    }

    /** Persists an attempt failure and returns its next scheduling decision. */
    private ProcessingDecision recordProcessingFailure(
            EventInboxEntity event,
            Exception processingFailure) {
        String message = processingFailure.getClass().getSimpleName()
                + COLON_SEPARATOR
                + (processingFailure.getMessage() == null ? PROCESSING_FAILED : processingFailure.getMessage());
        try {
            FailureTransition transition = processingStore.recordFailure(
                    event.getEventId(), message, Instant.now(clock));
            return transition.isDeadLettered()
                    ? ProcessingDecision.blocked()
                    : ProcessingDecision.waitFor(transition.getRetryDelay());
        } catch (RuntimeException persistenceFailure) {
            System.getLogger(getClass().getName()).log(
                    System.Logger.Level.ERROR,
                    FAILURE_PERSISTENCE_FAILED_PREFIX + event.getEventId(),
                    persistenceFailure);
            return ProcessingDecision.waitFor(uncertainStateDelay);
        }
    }

    /** Carries the prepared result before its transactional commit. */
    private static final class ProcessingCandidate {
        private final ProcessingOutcome outcome;
        private final String enrichedPayload;
        private final long tenantConfigVersion;
        private final Long preferenceVersion;

        /** Creates a processing candidate with its required state. */
        private ProcessingCandidate(
                ProcessingOutcome outcome,
                String enrichedPayload,
                long tenantConfigVersion,
                Long preferenceVersion) {
            this.outcome = outcome;
            this.enrichedPayload = enrichedPayload;
            this.tenantConfigVersion = tenantConfigVersion;
            this.preferenceVersion = preferenceVersion;
        }
    }
}
