package com.zingly.events.dlq.persistence.domain;

import static com.zingly.events.common.constants.DatabaseConstants.*;
import static com.zingly.events.common.constants.ErrorConstants.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
import java.time.Instant;
import java.util.Objects;

import com.zingly.events.dlq.DeadLetterStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = DEAD_LETTER_TABLE)
/** Maps persisted dead letter data to its database table. */
public class DeadLetterEntity {

    @Id
    @Column(name = DLQ_ID_COLUMN, nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID dlqId;

    @Column(name = EVENT_ID_COLUMN, nullable = false, length = 100)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID eventId;

    @Column(name = TENANT_ID_COLUMN, nullable = false, length = 100)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID tenantId;

    @Column(name = CUSTOMER_ID_COLUMN, nullable = false, length = 100)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID customerId;

    @Column(name = CUSTOMER_SEQUENCE_COLUMN, nullable = false)
    private long customerSequence;

    @Column(name = CYCLE_NUMBER_COLUMN, nullable = false)
    private int cycleNumber;

    @Column(name = FAILURE_COUNT_COLUMN, nullable = false)
    private int failureCount;

    @Lob
    @Column(name = FAILURE_HISTORY_COLUMN, nullable = false)
    private String failureHistory;

    @Column(name = LAST_ERROR_COLUMN, nullable = false, length = 2000)
    private String lastError;

    @Enumerated(EnumType.STRING)
    @Column(name = STATUS_PARAMETER, nullable = false, length = 20)
    private DeadLetterStatus status;

    @Column(name = CREATED_AT_COLUMN, nullable = false)
    private Instant createdAt;

    @Column(name = REPLAYED_AT_COLUMN)
    private Instant replayedAt;

    @Version
    @Column(name = VERSION_COLUMN, nullable = false)
    private long version;

    /** Provides the no-argument constructor required by JPA. */
    protected DeadLetterEntity() {
    }

    /** Creates a dead letter entity with its required state. */
    public DeadLetterEntity(
            UUID dlqId,
            UUID eventId,
            UUID tenantId,
            UUID customerId,
            long customerSequence,
            int cycleNumber,
            int failureCount,
            String failureHistory,
            String lastError,
            Instant createdAt) {
        this.dlqId = Objects.requireNonNull(dlqId, DLQ_ID_REQUIRED);
        this.eventId = Objects.requireNonNull(eventId, EVENT_ID_REQUIRED);
        this.tenantId = Objects.requireNonNull(tenantId, TENANT_ID_REQUIRED);
        this.customerId = Objects.requireNonNull(customerId, CUSTOMER_ID_REQUIRED);
        this.customerSequence = customerSequence;
        this.cycleNumber = cycleNumber;
        this.failureCount = failureCount;
        this.failureHistory = Objects.requireNonNull(failureHistory, FAILURE_HISTORY_REQUIRED);
        this.lastError = Objects.requireNonNull(lastError, LAST_ERROR_REQUIRED);
        this.status = DeadLetterStatus.ACTIVE;
        this.createdAt = Objects.requireNonNull(createdAt, CREATED_AT_REQUIRED);
    }

    /** Marks the entity as replayed. */
    public void markReplayed(Instant now) {
        status = DeadLetterStatus.REPLAYED;
        replayedAt = Objects.requireNonNull(now, NOW_REQUIRED);
    }

    /** Returns the dlq id. */
    public UUID getDlqId() { return dlqId; }
    /** Returns the event id. */
    public UUID getEventId() { return eventId; }
    /** Returns the tenant id. */
    public UUID getTenantId() { return tenantId; }
    /** Returns the customer id. */
    public UUID getCustomerId() { return customerId; }
    /** Returns the customer sequence. */
    public long getCustomerSequence() { return customerSequence; }
    /** Returns the cycle number. */
    public int getCycleNumber() { return cycleNumber; }
    /** Returns the failure count. */
    public int getFailureCount() { return failureCount; }
    /** Returns the failure history. */
    public String getFailureHistory() { return failureHistory; }
    /** Returns the last error. */
    public String getLastError() { return lastError; }
    /** Returns the current durable processing status of an event. */
    public DeadLetterStatus getStatus() { return status; }
    /** Returns the created at. */
    public Instant getCreatedAt() { return createdAt; }
    /** Returns the replayed at. */
    public Instant getReplayedAt() { return replayedAt; }
    /** Returns the version. */
    public long getVersion() { return version; }
}
