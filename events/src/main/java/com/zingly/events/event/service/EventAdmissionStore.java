package com.zingly.events.event.service;

import java.time.Instant;

import com.zingly.events.event.controller.dto.CreateEventRequest;
import com.zingly.events.event.persistence.CustomerSequenceRepository;
import com.zingly.events.event.persistence.EventInboxRepository;
import com.zingly.events.event.persistence.domain.CustomerSequenceEntity;
import com.zingly.events.event.persistence.domain.CustomerSequenceId;
import com.zingly.events.event.persistence.domain.EventInboxEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/** Persists a new event and allocates its customer sequence atomically. */
public class EventAdmissionStore {

    private final CustomerSequenceRepository sequenceRepository;
    private final EventInboxRepository inboxRepository;

    /** Creates an event admission store with its required state. */
    public EventAdmissionStore(
            CustomerSequenceRepository sequenceRepository,
            EventInboxRepository inboxRepository) {
        this.sequenceRepository = sequenceRepository;
        this.inboxRepository = inboxRepository;
    }

    @Transactional
    /** Allocates a customer sequence and inserts the inbox event atomically. */
    public EventInboxEntity insert(
            CreateEventRequest request,
            String fingerprint,
            String payload,
            Instant receivedAt) {
        CustomerSequenceId sequenceId = new CustomerSequenceId(
                request.getTenantId(), request.getCustomerId());
        CustomerSequenceEntity sequence = sequenceRepository.findById(sequenceId)
                .orElseGet(() -> new CustomerSequenceEntity(sequenceId, receivedAt));
        long allocatedSequence = sequence.allocateNext(receivedAt);
        sequenceRepository.save(sequence);

        EventInboxEntity event = new EventInboxEntity(
                request.getEventId(),
                fingerprint,
                request.getTenantId(),
                request.getCustomerId(),
                request.getChannel(),
                request.getNotificationType(),
                payload,
                request.getTimestamp(),
                receivedAt,
                allocatedSequence);
        return inboxRepository.saveAndFlush(event);
    }
}
