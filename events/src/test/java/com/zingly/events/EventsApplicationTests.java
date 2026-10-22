package com.zingly.events;

import static com.zingly.events.common.constants.TestConstants.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.function.BooleanSupplier;

import com.zingly.events.common.exception.ConflictException;
import com.zingly.events.dlq.DeadLetterStatus;
import com.zingly.events.dlq.persistence.DeadLetterRepository;
import com.zingly.events.dlq.service.DeadLetterService;
import com.zingly.events.event.controller.dto.CreateEventRequest;
import com.zingly.events.event.domain.enums.Channel;
import com.zingly.events.event.domain.enums.EventStatus;
import com.zingly.events.event.domain.enums.ProcessingOutcome;
import com.zingly.events.event.persistence.ProcessedEventRepository;
import com.zingly.events.event.service.EventService;
import com.zingly.events.customer_preference.controller.dto.UpdatePreferenceRequest;
import com.zingly.events.customer_preference.persistence.PreferenceChangeLogRepository;
import com.zingly.events.customer_preference.service.PreferenceChangeLogService;
import com.zingly.events.customer_preference.service.UserPreferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(properties = {
        TEST_DATASOURCE_PROPERTY,
        DISABLE_H2_CONSOLE_PROPERTY,
        RETRY_INITIAL_DELAY_PROPERTY,
        RETRY_MAX_DELAY_PROPERTY,
        RETRY_JITTER_PROPERTY,
        TRANSIENT_FAILURE_RATE_PROPERTY,
        RECONCILIATION_INTERVAL_PROPERTY,
        STALE_RUNNING_THRESHOLD_PROPERTY
})
/** Verifies the main ordering, idempotency, preference, retry, and replay flow. */
class EventsApplicationTests {

    private static final UUID TENANT_ID = UUID.fromString(TENANT_UUID);
    private static final UUID CUSTOMER_ONE_ID = UUID.fromString(CUSTOMER_ONE_UUID);
    private static final UUID CUSTOMER_TWO_ID = UUID.fromString(CUSTOMER_TWO_UUID);
    private static final UUID EVENT_ONE_ID = UUID.fromString(EVENT_ONE_UUID);
    private static final UUID EVENT_TWO_ID = UUID.fromString(EVENT_TWO_UUID);
    private static final UUID POISON_EVENT_ID = UUID.fromString(POISON_EVENT_UUID);

    @Autowired
    private EventService eventService;

    @Autowired
    private UserPreferenceService preferenceService;

    @Autowired
    private DeadLetterService deadLetterService;

    @Autowired
    private ProcessedEventRepository processedRepository;

    @Autowired
    private DeadLetterRepository deadLetterRepository;

    @Autowired
    private PreferenceChangeLogRepository preferenceChangeLogRepository;

    @Autowired
    private PreferenceChangeLogService preferenceChangeLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    /** Performs the processes in sequence deduplicates honors preferences and replays dlq operation. */
    void processesInSequenceDeduplicatesHonorsPreferencesAndReplaysDlq() throws Exception {
        preferenceService.update(
                TENANT_ID,
                CUSTOMER_ONE_ID,
                UpdatePreferenceRequest.builder()
                        .notificationType(ORDER_STATUS_NOTIFICATION)
                        .channel(Channel.EMAIL)
                        .enabled(false)
                        .updatedBy(INTEGRATION_TEST_ACTOR)
                        .build());
        var preferenceChangeLog = preferenceChangeLogRepository.findAll().get(0);
        assertEquals(
                INITIAL_CHANGE_LOG,
                preferenceChangeLog.getChangeLog());
        var changeLogJson = preferenceChangeLogService
                .list(TENANT_ID, CUSTOMER_ONE_ID, preferenceChangeLog.getPreferenceId())
                .get(0)
                .getChangeLog();
        assertTrue(changeLogJson.get(PREVIOUS_VALUE_FIELD).isNull());
        assertFalse(changeLogJson.get(CURRENT_VALUE_FIELD).asBoolean());

        CreateEventRequest first = event(
                EVENT_ONE_ID, CUSTOMER_ONE_ID, Channel.CHAT, CHAT_PAYLOAD);
        CreateEventRequest second = event(
                EVENT_TWO_ID, CUSTOMER_ONE_ID, Channel.EMAIL, EMAIL_PAYLOAD);
        assertEquals(1, eventService.accept(first).getCustomerSequence());
        assertEquals(2, eventService.accept(second).getCustomerSequence());

        await(() -> processedRepository.existsById(EVENT_ONE_ID)
                && processedRepository.existsById(EVENT_TWO_ID), Duration.ofSeconds(5));
        assertEquals(
                ProcessingOutcome.DELIVERED,
                processedRepository.findById(EVENT_ONE_ID).orElseThrow().getOutcome());
        assertEquals(
                ProcessingOutcome.SUPPRESSED_BY_PREFERENCE,
                processedRepository.findById(EVENT_TWO_ID).orElseThrow().getOutcome());
        assertTrue(eventService.accept(first).isDuplicate());

        CreateEventRequest conflicting = event(
                EVENT_ONE_ID, CUSTOMER_ONE_ID, Channel.CHAT, CHANGED_CHAT_PAYLOAD);
        assertThrows(ConflictException.class, () -> eventService.accept(conflicting));

        CreateEventRequest poison = event(
                POISON_EVENT_ID, CUSTOMER_TWO_ID, Channel.VOICE,
                POISON_VOICE_PAYLOAD);
        eventService.accept(poison);
        await(() -> eventService.getStatus(POISON_EVENT_ID).getStatus() == EventStatus.DLQ,
                Duration.ofSeconds(5));
        var firstDeadLetter = deadLetterRepository
                .findByEventIdAndStatus(POISON_EVENT_ID, DeadLetterStatus.ACTIVE)
                .orElseThrow();
        deadLetterService.replay(firstDeadLetter.getDlqId());
        await(() -> deadLetterRepository.countByEventId(POISON_EVENT_ID) == 2,
                Duration.ofSeconds(5));
        assertEquals(EventStatus.DLQ, eventService.getStatus(POISON_EVENT_ID).getStatus());
    }

    /** Performs the event operation. */
    private CreateEventRequest event(
            UUID eventId,
            UUID customerId,
            Channel channel,
            String payload) throws Exception {
        return CreateEventRequest.builder()
                .eventId(eventId)
                .tenantId(TENANT_ID)
                .customerId(customerId)
                .channel(channel)
                .notificationType(ORDER_STATUS_NOTIFICATION)
                .payload(objectMapper.readTree(payload))
                .timestamp(Instant.parse(EVENT_TIME))
                .build();
    }

    /** Performs the await operation. */
    private void await(BooleanSupplier condition, Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        assertTrue(condition.getAsBoolean(), TIMEOUT_ASSERTION_PREFIX + timeout);
    }
}
