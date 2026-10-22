package com.zingly.events.tenant.persistence.domain;

import static com.zingly.events.common.constants.DatabaseConstants.*;
import static com.zingly.events.common.constants.ErrorConstants.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/** Represents a tenant independently from its per-channel configurations. */
@Entity
@Table(name = TENANT_ACCOUNT_TABLE)
public class TenantEntity {

    @Id
    @Column(name = TENANT_ID_COLUMN, nullable = false, length = 100)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID tenantId;

    @Column(name = CREATED_AT_COLUMN, nullable = false)
    private Instant createdAt;

    @Column(name = UPDATED_AT_COLUMN, nullable = false)
    private Instant updatedAt;

    /** Provides the no-argument constructor required by JPA. */
    protected TenantEntity() {
    }

    /** Creates a tenant account at the supplied time. */
    public TenantEntity(UUID tenantId, Instant now) {
        this.tenantId = Objects.requireNonNull(tenantId, TENANT_ID_REQUIRED);
        this.createdAt = Objects.requireNonNull(now, NOW_REQUIRED);
        this.updatedAt = now;
    }

    /** Returns the tenant identifier. */
    public UUID getTenantId() {
        return tenantId;
    }

    /** Returns when the tenant was created. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Returns when the tenant was last updated. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
