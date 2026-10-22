package com.zingly.events.processing.restore;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.zingly.events.config.ProcessingProperties;
import com.zingly.events.event.domain.enums.EventStatus;
import com.zingly.events.event.persistence.EventInboxRepository;
import com.zingly.events.event.persistence.ProcessedEventRepository;
import com.zingly.events.event.persistence.domain.EventInboxEntity;
import com.zingly.events.processing.coordinator.CustomerKey;
import com.zingly.events.processing.coordinator.CustomerQueueCoordinator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/** Restores interrupted work and reconciles persisted state with runtime scheduling. */
public class RestoreService {

    private static final EnumSet<EventStatus> UNRESOLVED = EnumSet.of(
            EventStatus.QUEUED,
            EventStatus.RUNNING,
            EventStatus.RETRY_WAIT,
            EventStatus.DLQ);

    private final EventInboxRepository inboxRepository;
    private final ProcessedEventRepository processedRepository;
    private final Clock clock;
    private final ProcessingProperties properties;
    private final CustomerQueueCoordinator coordinator;

    /** Creates a restore service with its required state. */
    public RestoreService(
            EventInboxRepository inboxRepository,
            ProcessedEventRepository processedRepository,
            Clock clock,
            ProcessingProperties properties,
            CustomerQueueCoordinator coordinator) {
        this.inboxRepository = inboxRepository;
        this.processedRepository = processedRepository;
        this.clock = clock;
        this.properties = properties;
        this.coordinator = coordinator;
    }

    @Transactional
    /** Rebuilds durable unresolved state after application startup. */
    public RestoreSnapshot restoreStartupState() {
        Instant now = Instant.now(clock);
        List<EventInboxEntity> unresolved = inboxRepository
                .findByStatusInOrderByTenantIdAscCustomerIdAscCustomerSequenceAsc(UNRESOLVED);
        for (EventInboxEntity event : unresolved) {
            if (event.getStatus() == EventStatus.RUNNING) {
                if (processedRepository.existsById(event.getEventId())) {
                    event.markProcessed(now);
                } else {
                    event.recoverInterruptedAttempt(now);
                }
            }
        }
        inboxRepository.flush();
        List<EventInboxEntity> remaining = inboxRepository
                .findByStatusInOrderByTenantIdAscCustomerIdAscCustomerSequenceAsc(UNRESOLVED);
        return new RestoreSnapshot(remaining.size(), customerKeys(remaining));
    }

    @Transactional
    /** Repairs stale claims and returns customer lanes that need scheduling. */
    public ReconcileSnapshot reconcile() {
        Instant now = Instant.now(clock);
        Instant staleBefore = now.minus(properties.restore().staleRunningThreshold());
        List<EventInboxEntity> stale = inboxRepository
                .findByStatusAndClaimedAtBeforeOrderByClaimedAtAsc(EventStatus.RUNNING, staleBefore);
        int completedEvents = 0;
        for (EventInboxEntity event : stale) {
            CustomerKey key = new CustomerKey(event.getTenantId(), event.getCustomerId());
            if (coordinator.isRunning(key)) {
                continue;
            }
            if (processedRepository.existsById(event.getEventId())) {
                event.markProcessed(now);
                completedEvents++;
            } else {
                event.recoverInterruptedAttempt(now);
            }
        }
        inboxRepository.flush();
        return new ReconcileSnapshot(
                completedEvents,
                customerKeys(inboxRepository
                        .findByStatusInOrderByTenantIdAscCustomerIdAscCustomerSequenceAsc(UNRESOLVED)));
    }

    /** Extracts schedulable customer heads from ordered unresolved events. */
    private Set<CustomerKey> customerKeys(List<EventInboxEntity> events) {
        Set<CustomerKey> keys = new LinkedHashSet<>();
        Set<CustomerKey> inspected = new LinkedHashSet<>();
        for (EventInboxEntity event : events) {
            CustomerKey key = new CustomerKey(event.getTenantId(), event.getCustomerId());
            if (inspected.add(key) && event.getStatus() != EventStatus.DLQ) {
                keys.add(key);
            }
        }
        return keys;
    }
}
