package com.zingly.events.event.persistence.domain;

import static com.zingly.events.common.constants.DatabaseConstants.*;
import static com.zingly.events.common.constants.ErrorConstants.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
import java.time.Instant;
import java.util.Objects;

import com.zingly.events.event.domain.enums.Channel;
import com.zingly.events.event.domain.enums.ProcessingOutcome;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = PROCESSED_EVENT_TABLE)
/** Maps persisted processed event data to its database table. */
public class ProcessedEventEntity {

    @Id
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

    @Enumerated(EnumType.STRING)
    @Column(name = CHANNEL_COLUMN, nullable = false, length = 20)
    private Channel channel;

    @Column(name = NOTIFICATION_TYPE_COLUMN, nullable = false, length = 100)
    private String notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = OUTCOME_COLUMN, nullable = false, length = 40)
    private ProcessingOutcome outcome;

    @Lob
    @Column(name = ORIGINAL_PAYLOAD_COLUMN, nullable = false)
    private String originalPayload;

    @Lob
    @Column(name = ENRICHED_PAYLOAD_COLUMN, nullable = false)
    private String enrichedPayload;

    @Column(name = TENANT_CONFIG_VERSION_COLUMN, nullable = false)
    private long tenantConfigVersion;

    @Column(name = PREFERENCE_VERSION_COLUMN)
    private Long preferenceVersion;

    @Column(name = PROCESSED_AT_COLUMN, nullable = false)
    private Instant processedAt;

    /** Provides the no-argument constructor required by JPA. */
    protected ProcessedEventEntity() {
    }

    /** Creates a processed event entity with its required state. */
    public ProcessedEventEntity(
            UUID eventId,
            UUID tenantId,
            UUID customerId,
            long customerSequence,
            Channel channel,
            String notificationType,
            ProcessingOutcome outcome,
            String originalPayload,
            String enrichedPayload,
            long tenantConfigVersion,
            Long preferenceVersion,
            Instant processedAt) {
        this.eventId = Objects.requireNonNull(eventId, EVENT_ID_REQUIRED);
        this.tenantId = Objects.requireNonNull(tenantId, TENANT_ID_REQUIRED);
        this.customerId = Objects.requireNonNull(customerId, CUSTOMER_ID_REQUIRED);
        this.customerSequence = customerSequence;
        this.channel = Objects.requireNonNull(channel, CHANNEL_REQUIRED);
        this.notificationType = Objects.requireNonNull(notificationType, NOTIFICATION_TYPE_REQUIRED);
        this.outcome = Objects.requireNonNull(outcome, OUTCOME_REQUIRED);
        this.originalPayload = Objects.requireNonNull(originalPayload, ORIGINAL_PAYLOAD_REQUIRED);
        this.enrichedPayload = Objects.requireNonNull(enrichedPayload, ENRICHED_PAYLOAD_REQUIRED);
        this.tenantConfigVersion = tenantConfigVersion;
        this.preferenceVersion = preferenceVersion;
        this.processedAt = Objects.requireNonNull(processedAt, PROCESSED_AT_REQUIRED);
    }

    /** Returns the event id. */
    public UUID getEventId() { return eventId; }
    /** Returns the tenant id. */
    public UUID getTenantId() { return tenantId; }
    /** Returns the customer id. */
    public UUID getCustomerId() { return customerId; }
    /** Returns the customer sequence. */
    public long getCustomerSequence() { return customerSequence; }
    /** Returns the channel. */
    public Channel getChannel() { return channel; }
    /** Returns the notification type. */
    public String getNotificationType() { return notificationType; }
    /** Returns the outcome. */
    public ProcessingOutcome getOutcome() { return outcome; }
    /** Returns the original payload. */
    public String getOriginalPayload() { return originalPayload; }
    /** Returns the enriched payload. */
    public String getEnrichedPayload() { return enrichedPayload; }
    /** Returns the tenant config version. */
    public long getTenantConfigVersion() { return tenantConfigVersion; }
    /** Returns the preference version. */
    public Long getPreferenceVersion() { return preferenceVersion; }
    /** Returns the processed at. */
    public Instant getProcessedAt() { return processedAt; }
}
