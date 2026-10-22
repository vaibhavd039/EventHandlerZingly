package com.zingly.events.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/** Groups the worker pool and retry scheduler used by processing components. */
public record WorkerExecutors(
        ExecutorService workerExecutor,
        ScheduledExecutorService retryScheduler) {
}
