package com.zingly.events.dlq.service;

import java.util.UUID;
import java.time.Clock;
import java.time.Instant;

import com.zingly.events.dlq.DeadLetterStatus;
import com.zingly.events.dlq.controller.dto.DeadLetterPageResponse;
import com.zingly.events.dlq.controller.dto.DeadLetterResponse;
import com.zingly.events.dlq.controller.dto.ReplayResponse;
import com.zingly.events.dlq.persistence.DeadLetterRepository;
import com.zingly.events.dlq.persistence.domain.DeadLetterEntity;
import com.zingly.events.dlq.service.dto.ReplayResult;
import com.zingly.events.processing.coordinator.CustomerQueueCoordinator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/** Lists dead letters and coordinates explicit replay requests. */
public class DeadLetterService {

    private final DeadLetterRepository repository;
    private final DeadLetterReplayStore replayStore;
    private final CustomerQueueCoordinator coordinator;
    private final Clock clock;

    /** Creates a dead letter service with its required state. */
    public DeadLetterService(
            DeadLetterRepository repository,
            DeadLetterReplayStore replayStore,
            CustomerQueueCoordinator coordinator,
            Clock clock) {
        this.repository = repository;
        this.replayStore = replayStore;
        this.coordinator = coordinator;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    /** Lists explicitly configured preferences for a tenant customer. */
    public DeadLetterPageResponse list(int page, int size, boolean includeReplayed) {
        PageRequest pageable = PageRequest.of(page, Math.min(Math.max(size, 1), 200));
        Page<DeadLetterEntity> result = includeReplayed
                ? repository.findAll(pageable)
                : repository.findByStatusOrderByCreatedAtAsc(DeadLetterStatus.ACTIVE, pageable);
        return DeadLetterPageResponse.builder()
                .items(result.getContent().stream().map(this::toResponse).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    /** Reactivates an eligible dead-lettered customer head. */
    public ReplayResponse replay(UUID dlqId) {
        ReplayResult result = replayStore.replay(dlqId, Instant.now(clock));
        if (!result.isAlreadyReplayed()) {
            coordinator.signal(result.getCustomerKey());
        }
        return ReplayResponse.builder()
                .dlqId(result.getDlqId())
                .eventId(result.getEventId())
                .eventStatus(result.getEventStatus())
                .alreadyReplayed(result.isAlreadyReplayed())
                .build();
    }

    /** Maps a persistence entity to its API response. */
    private DeadLetterResponse toResponse(DeadLetterEntity entity) {
        return DeadLetterResponse.builder()
                .dlqId(entity.getDlqId())
                .eventId(entity.getEventId())
                .tenantId(entity.getTenantId())
                .customerId(entity.getCustomerId())
                .customerSequence(entity.getCustomerSequence())
                .cycleNumber(entity.getCycleNumber())
                .failureCount(entity.getFailureCount())
                .failureHistory(entity.getFailureHistory())
                .lastError(entity.getLastError())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .replayedAt(entity.getReplayedAt())
                .build();
    }
}
