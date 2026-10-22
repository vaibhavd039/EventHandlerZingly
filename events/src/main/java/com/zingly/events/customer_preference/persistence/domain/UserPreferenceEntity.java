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
import jakarta.persistence.Version;

@Entity
@Table(name = USER_PREFERENCE_TABLE)
/** Maps persisted user preference data to its database table. */
public class UserPreferenceEntity {

    @Id
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

    @Column(name = ENABLED_COLUMN, nullable = false)
    private boolean enabled;

    @Version
    @Column(name = VERSION_COLUMN, nullable = false)
    private long version;

    @Column(name = CREATED_AT_COLUMN, nullable = false)
    private Instant createdAt;

    @Column(name = UPDATED_AT_COLUMN, nullable = false)
    private Instant updatedAt;

    @Column(name = UPDATED_BY_COLUMN, nullable = false, length = 100)
    private String updatedBy;

    /** Provides the no-argument constructor required by JPA. */
    protected UserPreferenceEntity() {
    }

    /** Creates a user preference entity with its required state. */
    public UserPreferenceEntity(
            UUID preferenceId,
            UUID tenantId,
            UUID customerId,
            String notificationType,
            Channel channel,
            boolean enabled,
            String updatedBy,
            Instant now) {
        this.preferenceId = Objects.requireNonNull(preferenceId, PREFERENCE_ID_REQUIRED);
        this.tenantId = Objects.requireNonNull(tenantId, TENANT_ID_REQUIRED);
        this.customerId = Objects.requireNonNull(customerId, CUSTOMER_ID_REQUIRED);
        this.notificationType = Objects.requireNonNull(notificationType, NOTIFICATION_TYPE_REQUIRED);
        this.channel = Objects.requireNonNull(channel, CHANNEL_REQUIRED);
        this.enabled = enabled;
        this.updatedBy = Objects.requireNonNull(updatedBy, UPDATED_BY_REQUIRED);
        this.createdAt = Objects.requireNonNull(now, NOW_REQUIRED);
        this.updatedAt = now;
    }

    /** Creates or updates a user preference and publishes its change. */
    public void update(boolean enabled, String updatedBy, Instant now) {
        this.enabled = enabled;
        this.updatedBy = Objects.requireNonNull(updatedBy, UPDATED_BY_REQUIRED);
        this.updatedAt = Objects.requireNonNull(now, NOW_REQUIRED);
    }

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
    /** Indicates whether enabled. */
    public boolean isEnabled() { return enabled; }
    /** Returns the version. */
    public long getVersion() { return version; }
    /** Returns the created at. */
    public Instant getCreatedAt() { return createdAt; }
    /** Returns the updated at. */
    public Instant getUpdatedAt() { return updatedAt; }
    /** Returns the updated by. */
    public String getUpdatedBy() { return updatedBy; }
}
