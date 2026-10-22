package com.zingly.events.customer_preference.observer;

import static com.zingly.events.common.constants.PreferenceConstants.*;
import java.util.UUID;

import com.zingly.events.customer_preference.persistence.PreferenceChangeLogRepository;
import com.zingly.events.customer_preference.persistence.domain.PreferenceChangeLogEntity;
import com.zingly.events.customer_preference.service.dto.PreferenceChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
/** Writes a JSON change log after a committed preference update. */
public class PreferenceChangeLogObserver {

    private final PreferenceChangeLogRepository repository;

    /** Creates a preference change log observer with its required dependencies. */
    public PreferenceChangeLogObserver(PreferenceChangeLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    /** Applies one committed preference change to this observer. */
    public void onPreferenceChanged(PreferenceChangedEvent event) {
        String previousValue = event.getOldEnabled() == null
                ? NULL_JSON_VALUE
                : event.getOldEnabled().toString();
        String changeLog = CHANGE_LOG_PREVIOUS_PREFIX + previousValue
                + CHANGE_LOG_CURRENT_INFIX + event.isNewEnabled() + JSON_OBJECT_SUFFIX;

        repository.save(new PreferenceChangeLogEntity(
                UUID.randomUUID(),
                event.getPreferenceId(),
                event.getTenantId(),
                event.getCustomerId(),
                event.getNotificationType(),
                event.getChannel(),
                changeLog,
                event.getPreferenceVersion(),
                event.getChangedBy(),
                event.getChangedAt()));
    }
}
