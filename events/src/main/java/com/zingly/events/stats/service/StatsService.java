package com.zingly.events.stats.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;

import com.zingly.events.dlq.DeadLetterStatus;
import com.zingly.events.dlq.persistence.DeadLetterRepository;
import com.zingly.events.event.domain.enums.Channel;
import com.zingly.events.event.domain.enums.EventStatus;
import com.zingly.events.event.persistence.EventInboxRepository;
import com.zingly.events.event.persistence.ProcessedEventRepository;
import com.zingly.events.processing.coordinator.CustomerQueueCoordinator;
import com.zingly.events.stats.dto.ChannelStatsResponse;
import com.zingly.events.stats.dto.StatsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/** Builds a snapshot of durable processing and runtime worker metrics. */
public class StatsService {

    private static final int WINDOW_SECONDS = 60;

    private final EventInboxRepository inboxRepository;
    private final ProcessedEventRepository processedRepository;
    private final DeadLetterRepository deadLetterRepository;
    private final CustomerQueueCoordinator coordinator;
    private final Clock clock;

    /** Creates a stats service with its required state. */
    public StatsService(
            EventInboxRepository inboxRepository,
            ProcessedEventRepository processedRepository,
            DeadLetterRepository deadLetterRepository,
            CustomerQueueCoordinator coordinator,
            Clock clock) {
        this.inboxRepository = inboxRepository;
        this.processedRepository = processedRepository;
        this.deadLetterRepository = deadLetterRepository;
        this.coordinator = coordinator;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    /** Builds the current processing statistics snapshot. */
    public StatsResponse snapshot() {
        Instant snapshotAt = Instant.now(clock);
        Instant windowStart = snapshotAt.minusSeconds(WINDOW_SECONDS);
        int inFlight = coordinator.inFlightCount();
        long running = inboxRepository.countByStatus(EventStatus.RUNNING);

        return StatsResponse.builder()
                .processed(processedRepository.count())
                .inFlight(inFlight)
                .retried(inboxRepository.countRetryStarts())
                .deadLettered(deadLetterRepository.count())
                .dlqSize(deadLetterRepository.countByStatus(DeadLetterStatus.ACTIVE))
                .queued(inboxRepository.countByStatus(EventStatus.QUEUED))
                .retryWaiting(inboxRepository.countByStatus(EventStatus.RETRY_WAIT))
                .blockedCustomers(inboxRepository.countCustomersByStatus(EventStatus.DLQ))
                .recoveryPending(Math.max(0, running - inFlight))
                .windowSeconds(WINDOW_SECONDS)
                .snapshotAt(snapshotAt)
                .perChannel(Arrays.stream(Channel.values())
                        .map(channel -> channelStats(channel, windowStart))
                        .toList())
                .build();
    }

    /** Calculates total and rolling throughput for one channel. */
    private ChannelStatsResponse channelStats(Channel channel, Instant windowStart) {
        long completedInWindow = processedRepository
                .countByChannelAndProcessedAtGreaterThanEqual(channel, windowStart);
        return ChannelStatsResponse.builder()
                .channel(channel)
                .processed(processedRepository.countByChannel(channel))
                .completedInWindow(completedInWindow)
                .successfulPerSecond(completedInWindow / (double) WINDOW_SECONDS)
                .build();
    }
}
