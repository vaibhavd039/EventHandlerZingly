package com.zingly.events.event.persistence.domain;

import static com.zingly.events.common.constants.DatabaseConstants.*;
import static com.zingly.events.common.constants.ErrorConstants.*;
import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = CUSTOMER_SEQUENCE_COLUMN)
/** Maps persisted customer sequence data to its database table. */
public class CustomerSequenceEntity {

    @EmbeddedId
    private CustomerSequenceId id;

    @Column(name = NEXT_SEQUENCE_COLUMN, nullable = false)
    private long nextSequence;

    @Column(name = UPDATED_AT_COLUMN, nullable = false)
    private Instant updatedAt;

    /** Provides the no-argument constructor required by JPA. */
    protected CustomerSequenceEntity() {
    }

    /** Creates a customer sequence entity with its required state. */
    public CustomerSequenceEntity(CustomerSequenceId id, Instant now) {
        this.id = Objects.requireNonNull(id, ID_REQUIRED);
        this.nextSequence = 1;
        this.updatedAt = Objects.requireNonNull(now, NOW_REQUIRED);
    }

    /** Allocates next. */
    public long allocateNext(Instant now) {
        long allocated = nextSequence;
        nextSequence++;
        updatedAt = Objects.requireNonNull(now, NOW_REQUIRED);
        return allocated;
    }

    /** Returns the id. */
    public CustomerSequenceId getId() {
        return id;
    }

    /** Returns the next sequence. */
    public long getNextSequence() {
        return nextSequence;
    }

    /** Returns the updated at. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
