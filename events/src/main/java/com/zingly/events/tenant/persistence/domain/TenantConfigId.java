package com.zingly.events.tenant.persistence.domain;

import static com.zingly.events.common.constants.DatabaseConstants.*;
import static com.zingly.events.common.constants.ErrorConstants.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
import com.zingly.events.event.domain.enums.Channel;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.util.Objects;

/** Identifies one tenant configuration row by tenant and channel. */
@Embeddable
public class TenantConfigId implements Serializable {

    @Column(name = TENANT_ID_COLUMN, nullable = false, length = 100)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = CHANNEL_TYPE_COLUMN, nullable = false, length = 50)
    private Channel channelType;

    /** Provides the no-argument constructor required by JPA. */
    protected TenantConfigId() {
    }

    /** Creates a tenant-channel configuration identifier. */
    public TenantConfigId(UUID tenantId, Channel channelType) {
        this.tenantId = Objects.requireNonNull(tenantId, TENANT_ID_REQUIRED);
        this.channelType = Objects.requireNonNull(channelType, CHANNEL_TYPE_REQUIRED);
    }

    /** Returns the tenant identifier. */
    public UUID getTenantId() {
        return tenantId;
    }

    /** Returns the configured channel. */
    public Channel getChannelType() {
        return channelType;
    }

    /** Compares tenant-channel identifiers by both key fields. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TenantConfigId that)) {
            return false;
        }
        return tenantId.equals(that.tenantId) && channelType == that.channelType;
    }

    /** Returns a stable hash for the composite identifier. */
    @Override
    public int hashCode() {
        return Objects.hash(tenantId, channelType);
    }
}
