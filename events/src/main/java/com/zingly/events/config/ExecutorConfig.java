package com.zingly.events.config;

import static com.zingly.events.common.constants.ConfigurationConstants.*;
import static com.zingly.events.common.constants.LoggingConstants.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ProcessingProperties.class)
/** Creates and configures worker and retry executor beans. */
public class ExecutorConfig {

    public static final String EVENT_WORKER_EXECUTOR = EVENT_WORKER_EXECUTOR_BEAN;
    public static final String RETRY_SCHEDULER = RETRY_SCHEDULER_BEAN;

    @Bean(name = EVENT_WORKER_EXECUTOR, destroyMethod = SHUTDOWN_METHOD)
    /** Creates the fixed-size pool that runs event workers. */
    ExecutorService eventWorkerExecutor(ProcessingProperties properties) {
        return Executors.newFixedThreadPool(
                properties.workerCount(),
                namedThreadFactory(EVENT_WORKER_THREAD_PREFIX));
    }

    @Bean(name = RETRY_SCHEDULER, destroyMethod = SHUTDOWN_METHOD)
    /** Creates the scheduler used for delayed retries. */
    ScheduledExecutorService retryScheduler(ProcessingProperties properties) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                properties.schedulerThreads(),
                namedThreadFactory(EVENT_RETRY_THREAD_PREFIX));
        executor.setRemoveOnCancelPolicy(true);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        return executor;
    }

    @Bean
    /** Combines executor beans into one injectable dependency. */
    WorkerExecutors workerExecutors(
            @Qualifier(EVENT_WORKER_EXECUTOR) ExecutorService workerExecutor,
            @Qualifier(RETRY_SCHEDULER) ScheduledExecutorService retryScheduler) {
        return new WorkerExecutors(workerExecutor, retryScheduler);
    }

    /** Creates consistently named daemon threads with error logging. */
    private static ThreadFactory namedThreadFactory(String prefix) {
        AtomicInteger sequence = new AtomicInteger();
        return task -> {
            Thread thread = new Thread(task, prefix + sequence.incrementAndGet());
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((failedThread, error) ->
                    System.getLogger(ExecutorConfig.class.getName()).log(
                            System.Logger.Level.ERROR,
                            UNCAUGHT_EXCEPTION_PREFIX + failedThread.getName(),
                            error));
            return thread;
        };
    }
}
