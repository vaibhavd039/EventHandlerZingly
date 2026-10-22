package com.zingly.events.processing.service;

import static com.zingly.events.common.constants.ErrorConstants.*;
import static com.zingly.events.common.constants.ProcessingConstants.*;
import com.zingly.events.common.exception.ApplicationStateException;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import com.zingly.events.dlq.persistence.DeadLetterRepository;
import com.zingly.events.dlq.persistence.domain.DeadLetterEntity;
import com.zingly.events.event.domain.enums.EventStatus;
import com.zingly.events.event.domain.enums.ProcessingOutcome;
import com.zingly.events.event.persistence.EventInboxRepository;
import com.zingly.events.event.persistence.ProcessedEventRepository;
import com.zingly.events.event.persistence.domain.EventInboxEntity;
import com.zingly.events.event.persistence.domain.ProcessedEventEntity;
import com.zingly.events.processing.coordinator.CustomerKey;
import com.zingly.events.processing.retry.RetryPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/** Performs transactional event claim, completion, retry, and DLQ transitions. */
public class EventProcessingStore {

    private static final EnumSet<EventStatus> UNRESOLVED = EnumSet.of(
            EventStatus.QUEUED,
            EventStatus.RUNNING,
            EventStatus.RETRY_WAIT,
            EventStatus.DLQ);

    private final EventInboxRepository inboxRepository;
    private final ProcessedEventRepository processedRepository;
    private final DeadLetterRepository deadLetterRepository;
    private final RetryPolicy retryPolicy;

    /** Creates an event processing store with its required state. */
    public EventProcessingStore(
            EventInboxRepository inboxRepository,
            ProcessedEventRepository processedRepository,
            DeadLetterRepository deadLetterRepository,
            RetryPolicy retryPolicy) {
        this.inboxRepository = inboxRepository;
        this.processedRepository = processedRepository;
        this.deadLetterRepository = deadLetterRepository;
        this.retryPolicy = retryPolicy;
    }

    @Transactional
    /** Locks and claims only the lowest unresolved event for a customer. */
    public ClaimResult claimHead(CustomerKey key, Instant now) {
        EventInboxEntity head = inboxRepository
                .findFirstByTenantIdAndCustomerIdAndStatusInOrderByCustomerSequenceAsc(
                        key.getTenantId(), key.getCustomerId(), UNRESOLVED)
                .orElse(null);
        if (head == null) {
            return ClaimResult.nothing();
        }
        if (head.getStatus() == EventStatus.DLQ) {
            return ClaimResult.blocked();
        }
        if (head.getStatus() == EventStatus.RUNNING) {
            return ClaimResult.nothing();
        }
        if (head.getStatus() == EventStatus.RETRY_WAIT
                && head.getNextAttemptAt() != null
                && head.getNextAttemptAt().isAfter(now)) {
            return ClaimResult.waitFor(Duration.between(now, head.getNextAttemptAt()));
        }
        head.claim(now);
        inboxRepository.saveAndFlush(head);
        return ClaimResult.claimed(head);
    }

    @Transactional
    /** Commits one idempotent processed result and marks its inbox event complete. */
    public void complete(
            EventInboxEntity attemptedEvent,
            ProcessingOutcome outcome,
            String enrichedPayload,
            long tenantConfigVersion,
            Long preferenceVersion,
            Instant processedAt) {
        EventInboxEntity current = inboxRepository.findByIdForUpdate(attemptedEvent.getEventId())
                .orElseThrow(() -> new ApplicationStateException(CLAIMED_EVENT_MISSING));
        if (current.getStatus() == EventStatus.PROCESSED) {
            return;
        }
        if (current.getStatus() != EventStatus.RUNNING) {
            throw new ApplicationStateException(EVENT_NOT_RUNNING_PREFIX + current.getEventId());
        }
        if (!processedRepository.existsById(current.getEventId())) {
            processedRepository.save(new ProcessedEventEntity(
                    current.getEventId(),
                    current.getTenantId(),
                    current.getCustomerId(),
                    current.getCustomerSequence(),
                    current.getChannel(),
                    current.getNotificationType(),
                    outcome,
                    current.getPayload(),
                    enrichedPayload,
                    tenantConfigVersion,
                    preferenceVersion,
                    processedAt));
        }
        current.markProcessed(processedAt);
        inboxRepository.save(current);
    }

    @Transactional
    /** Persists retry timing or atomically moves the event to the DLQ. */
    public FailureTransition recordFailure(UUID eventId, String error, Instant now) {
        EventInboxEntity current = inboxRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new ApplicationStateException(CLAIMED_EVENT_MISSING));
        if (current.getStatus() != EventStatus.RUNNING) {
            throw new ApplicationStateException(FAILURE_REQUIRES_RUNNING_EVENT);
        }

        String boundedError = error.length() <= 2000 ? error : error.substring(0, 2000);
        int failureNumber = current.getCycleFailures() + 1;
        if (retryPolicy.shouldDeadLetter(failureNumber)) {
            current.moveToDlq(boundedError, now);
            int cycleNumber = Math.toIntExact(deadLetterRepository.countByEventId(eventId) + 1);
            deadLetterRepository.save(new DeadLetterEntity(
                    UUID.randomUUID(),
                    current.getEventId(),
                    current.getTenantId(),
                    current.getCustomerId(),
                    current.getCustomerSequence(),
                    cycleNumber,
                    failureNumber,
                    FAILURE_CYCLE_PREFIX + cycleNumber + FAILURE_NUMBER_INFIX + failureNumber + COLON_SEPARATOR + boundedError,
                    boundedError,
                    now));
            return FailureTransition.deadLettered();
        }

        Duration delay = retryPolicy.delayFor(failureNumber);
        current.scheduleRetry(now.plus(delay), boundedError, now);
        return FailureTransition.retry(delay);
    }
}
