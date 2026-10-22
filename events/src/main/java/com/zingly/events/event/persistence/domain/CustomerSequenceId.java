package com.zingly.events.event.persistence.domain;

import static com.zingly.events.common.constants.DatabaseConstants.*;
import static com.zingly.events.common.constants.ErrorConstants.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
/** Represents the composite identity of customer sequence. */
public class CustomerSequenceId implements Serializable {

    @Column(name = TENANT_ID_COLUMN, nullable = false, length = 100)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID tenantId;

    @Column(name = CUSTOMER_ID_COLUMN, nullable = false, length = 100)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID customerId;

    /** Provides the no-argument constructor required by JPA. */
    protected CustomerSequenceId() {
    }

    /** Creates a customer sequence id with its required state. */
    public CustomerSequenceId(UUID tenantId, UUID customerId) {
        this.tenantId = Objects.requireNonNull(tenantId, TENANT_ID_REQUIRED);
        this.customerId = Objects.requireNonNull(customerId, CUSTOMER_ID_REQUIRED);
    }

    /** Returns the tenant id. */
    public UUID getTenantId() {
        return tenantId;
    }

    /** Returns the customer id. */
    public UUID getCustomerId() {
        return customerId;
    }

    @Override
    /** Performs the equals operation. */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CustomerSequenceId that)) {
            return false;
        }
        return tenantId.equals(that.tenantId) && customerId.equals(that.customerId);
    }

    @Override
    /** Checks whether h code. */
    public int hashCode() {
        return Objects.hash(tenantId, customerId);
    }
}
