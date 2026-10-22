package com.zingly.events.customer_preference.observer;

import static com.zingly.events.common.constants.PreferenceConstants.*;
import com.zingly.events.customer_preference.service.dto.PreferenceChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
/** Notifies routing logic that a preference decision changed. */
public class NotificationRoutingObserver {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    /** Applies one committed preference change to this observer. */
    public void onPreferenceChanged(PreferenceChangedEvent event) {
        System.getLogger(getClass().getName()).log(
                System.Logger.Level.DEBUG,
                ROUTING_REFRESHED_PREFIX + event.getTenantId() + ROUTING_KEY_SEPARATOR + event.getCustomerId());
    }
}
