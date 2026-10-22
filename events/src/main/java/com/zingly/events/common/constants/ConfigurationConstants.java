package com.zingly.events.common.constants;

/** Defines bean names, property keys, scheduler expressions, and validation values. */
public interface ConfigurationConstants {

    String EVENT_WORKER_EXECUTOR_BEAN = "eventWorkerExecutor";
    String RETRY_SCHEDULER_BEAN = "retryScheduler";
    String SHUTDOWN_METHOD = "shutdown";
    String EVENT_WORKER_THREAD_PREFIX = "event-worker-";
    String EVENT_RETRY_THREAD_PREFIX = "event-retry-";
    String PROCESSING_PROPERTIES_PREFIX = "app.processing";
    String DECIMAL_ZERO = "0.0";
    String DECIMAL_ONE = "1.0";
    String INITIAL_DELAY_FIELD = "initialDelay";
    String MAX_DELAY_FIELD = "maxDelay";
    String RECONCILIATION_INTERVAL_FIELD = "reconciliationInterval";
    String STALE_RUNNING_THRESHOLD_FIELD = "staleRunningThreshold";
    String RECONCILIATION_INTERVAL_EXPRESSION = "${app.processing.restore.reconciliation-interval:5s}";
}
