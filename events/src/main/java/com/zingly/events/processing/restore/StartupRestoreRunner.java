package com.zingly.events.processing.restore;

import com.zingly.events.processing.coordinator.BacklogTracker;
import com.zingly.events.processing.coordinator.CustomerQueueCoordinator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
/** Rebuilds backlog and scheduling state when the application starts. */
public class StartupRestoreRunner implements ApplicationRunner {

    private final RestoreService restoreService;
    private final BacklogTracker backlogTracker;
    private final CustomerQueueCoordinator coordinator;

    /** Creates a startup restore runner with its required state. */
    public StartupRestoreRunner(
            RestoreService restoreService,
            BacklogTracker backlogTracker,
            CustomerQueueCoordinator coordinator) {
        this.restoreService = restoreService;
        this.backlogTracker = backlogTracker;
        this.coordinator = coordinator;
    }

    @Override
    /** Runs startup restore before normal application use. */
    public void run(ApplicationArguments args) {
        RestoreSnapshot snapshot = restoreService.restoreStartupState();
        backlogTracker.restore(snapshot.getUnresolvedCount());
        snapshot.getCustomers().forEach(coordinator::signal);
    }
}
