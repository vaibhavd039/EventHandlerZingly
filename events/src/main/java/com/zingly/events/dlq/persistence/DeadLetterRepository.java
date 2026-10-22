package com.zingly.events.dlq.persistence;

import static com.zingly.events.common.constants.DatabaseConstants.*;
import java.util.UUID;
import java.util.Optional;

import com.zingly.events.dlq.DeadLetterStatus;
import com.zingly.events.dlq.persistence.domain.DeadLetterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

/** Provides database access for dead letter records. */
public interface DeadLetterRepository extends JpaRepository<DeadLetterEntity, UUID> {

    /** Finds by status order by created at asc from persistent storage. */
    Page<DeadLetterEntity> findByStatusOrderByCreatedAtAsc(
            DeadLetterStatus status,
            Pageable pageable);

    /** Finds by event id and status from persistent storage. */
    Optional<DeadLetterEntity> findByEventIdAndStatus(
            UUID eventId,
            DeadLetterStatus status);

    /** Counts by status in persistent storage. */
    long countByStatus(DeadLetterStatus status);

    /** Counts by event id in persistent storage. */
    long countByEventId(UUID eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(DEAD_LETTER_BY_ID_FOR_UPDATE_QUERY)
    /** Finds by dlq id for update from persistent storage. */
    Optional<DeadLetterEntity> findByDlqIdForUpdate(@Param(DLQ_ID_PARAMETER) UUID dlqId);
}
