package com.zingly.events.event.persistence;

import java.util.Optional;

import com.zingly.events.event.persistence.domain.CustomerSequenceEntity;
import com.zingly.events.event.persistence.domain.CustomerSequenceId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

/** Provides database access for customer sequence records. */
public interface CustomerSequenceRepository
        extends JpaRepository<CustomerSequenceEntity, CustomerSequenceId> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    /** Finds by id from persistent storage. */
    Optional<CustomerSequenceEntity> findById(CustomerSequenceId id);
}
