package com.zingly.events.customer_preference.persistence.domain;

import static com.zingly.events.common.constants.DatabaseConstants.*;
import static com.zingly.events.common.constants.ErrorConstants.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
import java.time.Instant;
import java.util.Objects;

import com.zingly.events.event.domain.enums.Channel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = PREFERENCE_CHANGE_LOG_TABLE)
/** Maps a persisted preference change log to its database table. */
public class PreferenceChangeLogEntity {

    @Id
    @Column(name = CHANGE_LOG_ID_COLUMN, nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID changeLogId;

    @Column(name = PREFERENCE_ID_COLUMN, nullable = false, length = 36)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID preferenceId;

    @Column(name = TENANT_ID_COLUMN, nullable = false, length = 100)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID tenantId;

    @Column(name = CUSTOMER_ID_COLUMN, nullable = false, length = 100)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID customerId;

    @Column(name = NOTIFICATION_TYPE_COLUMN, nullable = false, length = 100)
    private String notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = CHANNEL_COLUMN, nullable = false, length = 20)
    private Channel channel;

    @Column(name = CHANGE_LOG_COLUMN, nullable = false, length = 1000)
    private String changeLog;

    @Column(name = PREFERENCE_VERSION_COLUMN, nullable = false)
    private long preferenceVersion;

    @Column(name = CHANGED_BY_COLUMN, nullable = false, length = 100)
    private String changedBy;

    @Column(name = CHANGED_AT_COLUMN, nullable = false)
    private Instant changedAt;

    /** Provides the no-argument constructor required by JPA. */
    protected PreferenceChangeLogEntity() {
    }

    /** Creates a preference change log entity with its required state. */
    public PreferenceChangeLogEntity(
            UUID changeLogId,
            UUID preferenceId,
            UUID tenantId,
            UUID customerId,
            String notificationType,
            Channel channel,
            String changeLog,
            long preferenceVersion,
            String changedBy,
            Instant changedAt) {
        this.changeLogId = Objects.requireNonNull(changeLogId, CHANGE_LOG_ID_REQUIRED);
        this.preferenceId = Objects.requireNonNull(preferenceId, PREFERENCE_ID_REQUIRED);
        this.tenantId = Objects.requireNonNull(tenantId, TENANT_ID_REQUIRED);
        this.customerId = Objects.requireNonNull(customerId, CUSTOMER_ID_REQUIRED);
        this.notificationType = Objects.requireNonNull(notificationType, NOTIFICATION_TYPE_REQUIRED);
        this.channel = Objects.requireNonNull(channel, CHANNEL_REQUIRED);
        this.changeLog = Objects.requireNonNull(changeLog, CHANGE_LOG_REQUIRED);
        this.preferenceVersion = preferenceVersion;
        this.changedBy = Objects.requireNonNull(changedBy, CHANGED_BY_REQUIRED);
        this.changedAt = Objects.requireNonNull(changedAt, CHANGED_AT_REQUIRED);
    }

    /** Returns the change log id. */
    public UUID getChangeLogId() { return changeLogId; }
    /** Returns the preference id. */
    public UUID getPreferenceId() { return preferenceId; }
    /** Returns the tenant id. */
    public UUID getTenantId() { return tenantId; }
    /** Returns the customer id. */
    public UUID getCustomerId() { return customerId; }
    /** Returns the notification type. */
    public String getNotificationType() { return notificationType; }
    /** Returns the channel. */
    public Channel getChannel() { return channel; }
    /** Returns the serialized JSON change log. */
    public String getChangeLog() { return changeLog; }
    /** Returns the preference version. */
    public long getPreferenceVersion() { return preferenceVersion; }
    /** Returns the changed by. */
    public String getChangedBy() { return changedBy; }
    /** Returns the changed at. */
    public Instant getChangedAt() { return changedAt; }
}
