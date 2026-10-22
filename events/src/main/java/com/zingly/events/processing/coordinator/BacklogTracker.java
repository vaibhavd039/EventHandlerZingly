package com.zingly.events.processing.coordinator;

import java.util.concurrent.atomic.AtomicInteger;

import com.zingly.events.config.ProcessingProperties;
import org.springframework.stereotype.Component;

@Component
/** Tracks unresolved-event capacity for admission control. */
public class BacklogTracker {

    private final int maximum;
    private final AtomicInteger unresolved = new AtomicInteger();

    /** Creates a backlog tracker with its required state. */
    public BacklogTracker(ProcessingProperties properties) {
        this.maximum = properties.backlog().maxUnresolvedEvents();
    }

    /** Atomically reserves one unresolved-event capacity slot. */
    public boolean tryReserve() {
        while (true) {
            int current = unresolved.get();
            if (current >= maximum) {
                return false;
            }
            if (unresolved.compareAndSet(current, current + 1)) {
                return true;
            }
        }
    }

    /** Releases one unresolved-event capacity slot. */
    public void release() {
        unresolved.updateAndGet(value -> Math.max(0, value - 1));
    }

    /** Replaces the runtime backlog count with persisted startup state. */
    public void restore(long persistedUnresolved) {
        unresolved.set(Math.toIntExact(Math.min(Integer.MAX_VALUE, persistedUnresolved)));
    }

    /** Returns the current unresolved-event count. */
    public int current() {
        return unresolved.get();
    }
}
