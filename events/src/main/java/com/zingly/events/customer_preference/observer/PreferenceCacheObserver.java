package com.zingly.events.customer_preference.observer;

import com.zingly.events.customer_preference.service.PreferenceCache;
import com.zingly.events.customer_preference.service.dto.PreferenceChangedEvent;
import com.zingly.events.customer_preference.service.dto.PreferenceDecision;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
/** Refreshes the preference cache after a committed preference change. */
public class PreferenceCacheObserver {

    private final PreferenceCache cache;

    /** Creates a preference cache observer with its required state. */
    public PreferenceCacheObserver(PreferenceCache cache) {
        this.cache = cache;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    /** Applies one committed preference change to this observer. */
    public void onPreferenceChanged(PreferenceChangedEvent event) {
        cache.put(event.key(), new PreferenceDecision(
                event.isNewEnabled(), event.getPreferenceVersion(), true));
    }
}
