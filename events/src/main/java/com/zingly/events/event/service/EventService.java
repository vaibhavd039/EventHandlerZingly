package com.zingly.events.event.service;

import static com.zingly.events.common.constants.ErrorConstants.*;
import com.zingly.events.common.exception.ApplicationValidationException;
import java.util.UUID;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import com.zingly.events.common.exception.BacklogFullException;
import com.zingly.events.common.exception.ResourceNotFoundException;
import com.zingly.events.event.controller.dto.CreateEventRequest;
import com.zingly.events.event.controller.dto.EventAcceptedResponse;
import com.zingly.events.event.controller.dto.EventStatusResponse;
import com.zingly.events.event.persistence.EventInboxRepository;
import com.zingly.events.event.persistence.domain.EventInboxEntity;
import com.zingly.events.event.util.EventDuplicateValidator;
import com.zingly.events.event.util.EventFingerprintUtil;
import com.zingly.events.processing.coordinator.BacklogTracker;
import com.zingly.events.processing.coordinator.CustomerKey;
import com.zingly.events.processing.coordinator.CustomerQueueCoordinator;
import com.zingly.events.tenant.service.TenantConfigService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
/** Handles event admission, idempotency, status lookup, and scheduling. */
public class EventService {

    private final EventInboxRepository inboxRepository;
    private final EventAdmissionStore admissionStore;
    private final EventDuplicateValidator duplicateValidator;
    private final TenantConfigService tenantConfigService;
    private final CustomerQueueCoordinator coordinator;
    private final BacklogTracker backlogTracker;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final ConcurrentMap<CustomerKey, ReentrantLock> admissionLocks = new ConcurrentHashMap<>();

    /** Creates an event service with its required state. */
    public EventService(
            EventInboxRepository inboxRepository,
            EventAdmissionStore admissionStore,
            EventDuplicateValidator duplicateValidator,
            TenantConfigService tenantConfigService,
            CustomerQueueCoordinator coordinator,
            BacklogTracker backlogTracker,
            ObjectMapper objectMapper,
            Clock clock) {
        this.inboxRepository = inboxRepository;
        this.admissionStore = admissionStore;
        this.duplicateValidator = duplicateValidator;
        this.tenantConfigService = tenantConfigService;
        this.coordinator = coordinator;
        this.backlogTracker = backlogTracker;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    /** Validates idempotency, persists a new event, and schedules its customer. */
    public EventAcceptedResponse accept(CreateEventRequest request) {
        String fingerprint = EventFingerprintUtil.fingerprint(request);
        EventInboxEntity existing = inboxRepository.findById(request.getEventId()).orElse(null);
        if (existing != null) {
            return duplicateValidator.validateAndCreateResponse(existing, fingerprint, request);
        }
        if (!backlogTracker.tryReserve()) {
            throw new BacklogFullException(BACKLOG_REACHED);
        }

        boolean accepted = false;
        try {
            tenantConfigService.ensureTenant(request.getTenantId());
            CustomerKey customerKey = new CustomerKey(request.getTenantId(), request.getCustomerId());
            ReentrantLock admissionLock = admissionLocks.computeIfAbsent(
                    customerKey, ignored -> new ReentrantLock());
            EventInboxEntity saved;
            admissionLock.lock();
            try {
                EventInboxEntity concurrentExisting = inboxRepository.findById(request.getEventId()).orElse(null);
                if (concurrentExisting != null) {
                    return duplicateValidator.validateAndCreateResponse(
                            concurrentExisting, fingerprint, request);
                }
                saved = admissionStore.insert(
                        request,
                        fingerprint,
                        objectMapper.writeValueAsString(request.getPayload()),
                        Instant.now(clock));
            } finally {
                admissionLock.unlock();
            }
            accepted = true;
            coordinator.signal(customerKey);
            return EventAcceptedResponse.from(saved, false);
        } catch (DataIntegrityViolationException duplicateRace) {
            EventInboxEntity winner = inboxRepository.findById(request.getEventId()).orElseThrow(() -> duplicateRace);
            return duplicateValidator.validateAndCreateResponse(winner, fingerprint, request);
        } catch (JacksonException serializationFailure) {
            throw new ApplicationValidationException(PAYLOAD_SERIALIZATION_FAILED, serializationFailure);
        } finally {
            if (!accepted) {
                backlogTracker.release();
            }
        }
    }

    /** Returns the current durable processing status of an event. */
    public EventStatusResponse getStatus(UUID eventId) {
        EventInboxEntity event = inboxRepository.findById(eventId).orElseThrow(() ->
                new ResourceNotFoundException(EVENT_NOT_FOUND_PREFIX + eventId));
        return EventStatusResponse.builder()
                .eventId(event.getEventId())
                .status(event.getStatus())
                .customerSequence(event.getCustomerSequence())
                .attemptStarts(event.getAttemptStarts())
                .cycleFailures(event.getCycleFailures())
                .nextAttemptAt(event.getNextAttemptAt())
                .lastError(event.getLastError())
                .statusUpdatedAt(event.getStatusUpdatedAt())
                .build();
    }

}
