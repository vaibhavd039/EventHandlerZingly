package com.zingly.events.event.persistence;

import java.time.Instant;
import java.util.UUID;

import com.zingly.events.event.domain.enums.Channel;
import com.zingly.events.event.persistence.domain.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Provides database access for processed event records. */
public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, UUID> {

    /** Counts by channel in persistent storage. */
    long countByChannel(Channel channel);

    /** Counts by channel and processed at greater than equal in persistent storage. */
    long countByChannelAndProcessedAtGreaterThanEqual(Channel channel, Instant windowStart);
}
