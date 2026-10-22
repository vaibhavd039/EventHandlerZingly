package com.zingly.events.event.persistence;

import static com.zingly.events.common.constants.DatabaseConstants.*;
import java.util.UUID;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.zingly.events.event.domain.enums.EventStatus;
import com.zingly.events.event.persistence.domain.EventInboxEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Provides database access for event inbox records. */
public interface EventInboxRepository extends JpaRepository<EventInboxEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    /** Finds first by tenant id and customer id and status in order by customer sequence asc from persistent storage. */
    Optional<EventInboxEntity> findFirstByTenantIdAndCustomerIdAndStatusInOrderByCustomerSequenceAsc(
            UUID tenantId,
            UUID customerId,
            Collection<EventStatus> statuses);

    /** Finds by status in order by tenant id asc customer id asc customer sequence asc from persistent storage. */
    List<EventInboxEntity> findByStatusInOrderByTenantIdAscCustomerIdAscCustomerSequenceAsc(
            Collection<EventStatus> statuses);

    /** Finds by status and next attempt at less than equal order by next attempt at asc from persistent storage. */
    List<EventInboxEntity> findByStatusAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
            EventStatus status,
            Instant dueAt,
            Pageable pageable);

    /** Counts by status in persistent storage. */
    long countByStatus(EventStatus status);

    /** Counts events matching any supplied status. */
    long countByStatusIn(Collection<EventStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(EVENT_BY_ID_FOR_UPDATE_QUERY)
    /** Finds by id for update from persistent storage. */
    Optional<EventInboxEntity> findByIdForUpdate(@Param(EVENT_ID_PARAMETER) UUID eventId);

    /** Finds by status and claimed at before order by claimed at asc from persistent storage. */
    List<EventInboxEntity> findByStatusAndClaimedAtBeforeOrderByClaimedAtAsc(
            EventStatus status,
            Instant claimedBefore);

    @Query(RETRY_COUNT_QUERY_SELECT
            + EVENT_INBOX_QUERY_FROM)
    /** Counts retry starts in persistent storage. */
    long countRetryStarts();

    @Query(ACTIVE_CUSTOMER_COUNT_QUERY_SELECT
            + ACTIVE_CUSTOMER_COUNT_QUERY_FROM)
    /** Counts customers by status in persistent storage. */
    long countCustomersByStatus(@Param(STATUS_PARAMETER) EventStatus status);
}
