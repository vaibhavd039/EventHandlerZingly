package com.zingly.events.event.persistence.domain;

import static com.zingly.events.common.constants.DatabaseConstants.*;
import static com.zingly.events.common.constants.ErrorConstants.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
import java.time.Instant;
import java.util.Objects;

import com.zingly.events.event.domain.enums.Channel;
import com.zingly.events.event.domain.enums.EventStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = EVENT_INBOX_TABLE)
/** Maps persisted event inbox data to its database table. */
public class EventInboxEntity {

    @Id
    @Column(name = EVENT_ID_COLUMN, nullable = false, length = 100)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID eventId;

    @Column(name = CONTENT_FINGERPRINT_COLUMN, nullable = false, length = 64, columnDefinition = CHAR_64_DEFINITION)
    private String contentFingerprint;

    @Column(name = TENANT_ID_COLUMN, nullable = false, length = 100)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID tenantId;

    @Column(name = CUSTOMER_ID_COLUMN, nullable = false, length = 100)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = CHANNEL_COLUMN, nullable = false, length = 20)
    private Channel channel;

    @Column(name = NOTIFICATION_TYPE_COLUMN, nullable = false, length = 100)
    private String notificationType;

    @Lob
    @Column(name = PAYLOAD_COLUMN, nullable = false)
    private String payload;

    @Column(name = EVENT_TIMESTAMP_COLUMN, nullable = false)
    private Instant eventTimestamp;

    @Column(name = RECEIVED_AT_COLUMN, nullable = false)
    private Instant receivedAt;

    @Column(name = CUSTOMER_SEQUENCE_COLUMN, nullable = false)
    private long customerSequence;

    @Enumerated(EnumType.STRING)
    @Column(name = STATUS_PARAMETER, nullable = false, length = 30)
    private EventStatus status;

    @Column(name = ATTEMPT_STARTS_COLUMN, nullable = false)
    private int attemptStarts;

    @Column(name = CYCLE_FAILURES_COLUMN, nullable = false)
    private int cycleFailures;

    @Column(name = NEXT_ATTEMPT_AT_COLUMN)
    private Instant nextAttemptAt;

    @Column(name = CLAIMED_AT_COLUMN)
    private Instant claimedAt;

    @Column(name = STATUS_UPDATED_AT_COLUMN, nullable = false)
    private Instant statusUpdatedAt;

    @Column(name = LAST_ERROR_COLUMN, length = 2000)
    private String lastError;

    @Version
    @Column(name = VERSION_COLUMN, nullable = false)
    private long version;

    /** Provides the no-argument constructor required by JPA. */
    protected EventInboxEntity() {
    }

    /** Creates an event inbox entity with its required state. */
    public EventInboxEntity(
            UUID eventId,
            String contentFingerprint,
            UUID tenantId,
            UUID customerId,
            Channel channel,
            String notificationType,
            String payload,
            Instant eventTimestamp,
            Instant receivedAt,
            long customerSequence) {
        this.eventId = Objects.requireNonNull(eventId, EVENT_ID_REQUIRED);
        this.contentFingerprint = Objects.requireNonNull(contentFingerprint, CONTENT_FINGERPRINT_REQUIRED);
        this.tenantId = Objects.requireNonNull(tenantId, TENANT_ID_REQUIRED);
        this.customerId = Objects.requireNonNull(customerId, CUSTOMER_ID_REQUIRED);
        this.channel = Objects.requireNonNull(channel, CHANNEL_REQUIRED);
        this.notificationType = Objects.requireNonNull(notificationType, NOTIFICATION_TYPE_REQUIRED);
        this.payload = Objects.requireNonNull(payload, PAYLOAD_REQUIRED);
        this.eventTimestamp = Objects.requireNonNull(eventTimestamp, EVENT_TIMESTAMP_REQUIRED);
        this.receivedAt = Objects.requireNonNull(receivedAt, RECEIVED_AT_REQUIRED);
        this.customerSequence = customerSequence;
        this.status = EventStatus.QUEUED;
        this.statusUpdatedAt = receivedAt;
    }

    /** Marks the event running and records a new attempt start. */
    public void claim(Instant now) {
        status = EventStatus.RUNNING;
        attemptStarts++;
        claimedAt = Objects.requireNonNull(now, NOW_REQUIRED);
        statusUpdatedAt = now;
        nextAttemptAt = null;
    }

    /** Moves the failed event into retry wait with its next due time. */
    public void scheduleRetry(Instant retryAt, String error, Instant now) {
        status = EventStatus.RETRY_WAIT;
        cycleFailures++;
        nextAttemptAt = Objects.requireNonNull(retryAt, RETRY_AT_REQUIRED);
        lastError = error;
        claimedAt = null;
        statusUpdatedAt = Objects.requireNonNull(now, NOW_REQUIRED);
    }

    /** Marks the entity as processed. */
    public void markProcessed(Instant now) {
        status = EventStatus.PROCESSED;
        nextAttemptAt = null;
        claimedAt = null;
        lastError = null;
        statusUpdatedAt = Objects.requireNonNull(now, NOW_REQUIRED);
    }

    /** Moves the event into DLQ state after retry exhaustion. */
    public void moveToDlq(String error, Instant now) {
        status = EventStatus.DLQ;
        cycleFailures++;
        nextAttemptAt = null;
        claimedAt = null;
        lastError = error;
        statusUpdatedAt = Objects.requireNonNull(now, NOW_REQUIRED);
    }

    /** Resets the dead-lettered event for explicit replay. */
    public void resetForReplay(Instant now) {
        status = EventStatus.QUEUED;
        cycleFailures = 0;
        nextAttemptAt = null;
        claimedAt = null;
        lastError = null;
        statusUpdatedAt = Objects.requireNonNull(now, NOW_REQUIRED);
    }

    /** Recovers interrupted attempt. */
    public void recoverInterruptedAttempt(Instant now) {
        status = EventStatus.QUEUED;
        claimedAt = null;
        nextAttemptAt = null;
        statusUpdatedAt = Objects.requireNonNull(now, NOW_REQUIRED);
    }

    /** Returns the event id. */
    public UUID getEventId() { return eventId; }
    /** Returns the content fingerprint. */
    public String getContentFingerprint() { return contentFingerprint; }
    /** Returns the tenant id. */
    public UUID getTenantId() { return tenantId; }
    /** Returns the customer id. */
    public UUID getCustomerId() { return customerId; }
    /** Returns the channel. */
    public Channel getChannel() { return channel; }
    /** Returns the notification type. */
    public String getNotificationType() { return notificationType; }
    /** Returns the payload. */
    public String getPayload() { return payload; }
    /** Returns the event timestamp. */
    public Instant getEventTimestamp() { return eventTimestamp; }
    /** Returns when the event was received. */
    public Instant getReceivedAt() { return receivedAt; }
    /** Returns the customer sequence. */
    public long getCustomerSequence() { return customerSequence; }
    /** Returns the current durable processing status of an event. */
    public EventStatus getStatus() { return status; }
    /** Returns the attempt starts. */
    public int getAttemptStarts() { return attemptStarts; }
    /** Returns the cycle failures. */
    public int getCycleFailures() { return cycleFailures; }
    /** Returns when the next retry becomes eligible. */
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    /** Returns when the current processing attempt was claimed. */
    public Instant getClaimedAt() { return claimedAt; }
    /** Returns when the event status last changed. */
    public Instant getStatusUpdatedAt() { return statusUpdatedAt; }
    /** Returns the last error. */
    public String getLastError() { return lastError; }
    /** Returns the version. */
    public long getVersion() { return version; }
}
