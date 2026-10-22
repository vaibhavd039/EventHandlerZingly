package com.zingly.events.dlq.service;

import static com.zingly.events.common.constants.ErrorConstants.*;
import java.util.UUID;
import java.time.Instant;
import java.util.EnumSet;

import com.zingly.events.common.exception.ConflictException;
import com.zingly.events.common.exception.ResourceNotFoundException;
import com.zingly.events.dlq.DeadLetterStatus;
import com.zingly.events.dlq.persistence.DeadLetterRepository;
import com.zingly.events.dlq.persistence.domain.DeadLetterEntity;
import com.zingly.events.dlq.service.dto.ReplayResult;
import com.zingly.events.event.domain.enums.EventStatus;
import com.zingly.events.event.persistence.EventInboxRepository;
import com.zingly.events.event.persistence.domain.EventInboxEntity;
import com.zingly.events.processing.coordinator.CustomerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/** Replays a dead-lettered queue head in one database transaction. */
public class DeadLetterReplayStore {

    private final DeadLetterRepository deadLetterRepository;
    private final EventInboxRepository inboxRepository;

    /** Creates a dead letter replay store with its required state. */
    public DeadLetterReplayStore(
            DeadLetterRepository deadLetterRepository,
            EventInboxRepository inboxRepository) {
        this.deadLetterRepository = deadLetterRepository;
        this.inboxRepository = inboxRepository;
    }

    @Transactional
    /** Reactivates an eligible dead-lettered customer head. */
    public ReplayResult replay(UUID dlqId, Instant now) {
        DeadLetterEntity deadLetter = deadLetterRepository.findByDlqIdForUpdate(dlqId)
                .orElseThrow(() -> new ResourceNotFoundException(DEAD_LETTER_NOT_FOUND_PREFIX + dlqId));
        CustomerKey key = new CustomerKey(deadLetter.getTenantId(), deadLetter.getCustomerId());
        if (deadLetter.getStatus() == DeadLetterStatus.REPLAYED) {
            EventStatus currentStatus = inboxRepository.findById(deadLetter.getEventId())
                    .map(EventInboxEntity::getStatus)
                    .orElse(EventStatus.QUEUED);
            return new ReplayResult(dlqId, deadLetter.getEventId(), currentStatus, true, key);
        }

        EventInboxEntity event = inboxRepository.findByIdForUpdate(deadLetter.getEventId())
                .orElseThrow(() -> new ConflictException(DEAD_LETTER_EVENT_MISSING));
        EventInboxEntity head = inboxRepository
                .findFirstByTenantIdAndCustomerIdAndStatusInOrderByCustomerSequenceAsc(
                        deadLetter.getTenantId(),
                        deadLetter.getCustomerId(),
                        EnumSet.of(EventStatus.QUEUED, EventStatus.RUNNING, EventStatus.RETRY_WAIT, EventStatus.DLQ))
                .orElseThrow(() -> new ConflictException(CUSTOMER_HEAD_MISSING));
        if (event.getStatus() != EventStatus.DLQ
                || event.getCustomerSequence() != deadLetter.getCustomerSequence()
                || !head.getEventId().equals(event.getEventId())) {
            throw new ConflictException(DEAD_LETTER_NOT_ACTIVE_HEAD);
        }
        event.resetForReplay(now);
        deadLetter.markReplayed(now);
        inboxRepository.save(event);
        deadLetterRepository.save(deadLetter);
        return new ReplayResult(dlqId, event.getEventId(), event.getStatus(), false, key);
    }
}
