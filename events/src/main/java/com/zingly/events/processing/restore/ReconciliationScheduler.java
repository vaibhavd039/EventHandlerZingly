package com.zingly.events.processing.restore;

import static com.zingly.events.common.constants.ConfigurationConstants.*;
import com.zingly.events.processing.coordinator.CustomerQueueCoordinator;
import com.zingly.events.processing.coordinator.BacklogTracker;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
/** Periodically repairs missed scheduling and stale persisted claims. */
public class ReconciliationScheduler {

    private final RestoreService restoreService;
    private final CustomerQueueCoordinator coordinator;
    private final BacklogTracker backlogTracker;

    /** Creates a reconciliation scheduler with its required state. */
    public ReconciliationScheduler(
            RestoreService restoreService,
            CustomerQueueCoordinator coordinator,
            BacklogTracker backlogTracker) {
        this.restoreService = restoreService;
        this.coordinator = coordinator;
        this.backlogTracker = backlogTracker;
    }

    @Scheduled(
            fixedDelayString = RECONCILIATION_INTERVAL_EXPRESSION,
            initialDelayString = RECONCILIATION_INTERVAL_EXPRESSION)
    /** Repairs stale claims and returns customer lanes that need scheduling. */
    public void reconcile() {
        ReconcileSnapshot snapshot = restoreService.reconcile();
        for (int index = 0; index < snapshot.getCompletedEvents(); index++) {
            backlogTracker.release();
        }
        snapshot.getCustomers().forEach(coordinator::signal);
    }
}
