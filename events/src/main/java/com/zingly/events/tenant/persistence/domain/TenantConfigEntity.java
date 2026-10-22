package com.zingly.events.tenant.persistence.domain;

import static com.zingly.events.common.constants.DatabaseConstants.*;
import static com.zingly.events.common.constants.ErrorConstants.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

import com.zingly.events.event.domain.enums.Channel;

/** Maps one tenant-channel enablement configuration to its database row. */
@Entity
@Table(name = TENANT_CONFIG_TABLE)
@IdClass(TenantConfigId.class)
public class TenantConfigEntity {

    @Id
    @Column(name = TENANT_ID_COLUMN, nullable = false, length = 100)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID tenantId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = CHANNEL_TYPE_COLUMN, nullable = false, length = 50)
    private Channel channelType;

    @Column(name = IS_ENABLED_COLUMN, nullable = false)
    private boolean isEnabled;

    @Column(name = CONFIG_VERSION_COLUMN, nullable = false)
    private long configVersion;

    @Column(name = CREATED_AT_COLUMN, nullable = false)
    private Instant createdAt;

    @Column(name = UPDATED_AT_COLUMN, nullable = false)
    private Instant updatedAt;

    /** Provides the no-argument constructor required by JPA. */
    protected TenantConfigEntity() {
    }

    /** Creates an initial configuration for one tenant channel. */
    public TenantConfigEntity(UUID tenantId, Channel channelType, boolean enabled, Instant now) {
        this.tenantId = Objects.requireNonNull(tenantId, TENANT_ID_REQUIRED);
        this.channelType = Objects.requireNonNull(channelType, CHANNEL_TYPE_REQUIRED);
        this.isEnabled = enabled;
        this.configVersion = 1;
        this.createdAt = Objects.requireNonNull(now, NOW_REQUIRED);
        this.updatedAt = now;
    }

    /** Updates channel enablement and increments its configuration version. */
    public void update(boolean enabled, Instant now) {
        this.isEnabled = enabled;
        this.configVersion++;
        this.updatedAt = Objects.requireNonNull(now, NOW_REQUIRED);
    }

    /** Returns the tenant identifier. */
    public UUID getTenantId() {
        return tenantId;
    }

    /** Returns the configured channel. */
    public Channel getChannelType() {
        return channelType;
    }

    /** Indicates whether the tenant permits this channel. */
    public boolean isEnabled() {
        return isEnabled;
    }

    /** Returns the version of this channel configuration. */
    public long getConfigVersion() {
        return configVersion;
    }

    /** Returns when this channel configuration was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Returns when this channel configuration was last updated. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
