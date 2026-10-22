package com.zingly.events.processing.coordinator;

import static com.zingly.events.common.constants.LoggingConstants.*;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.zingly.events.config.ProcessingProperties;
import com.zingly.events.config.WorkerExecutors;
import com.zingly.events.processing.service.EventWorker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
/** Serializes each customer lane while running different customers in parallel. */
public class CustomerQueueCoordinator {

    private final BlockingQueue<CustomerKey> readyCustomers;
    private final ConcurrentMap<CustomerKey, LaneState> lanes = new ConcurrentHashMap<>();
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final AtomicInteger inFlight = new AtomicInteger();
    private final int workerCount;
    private final WorkerExecutors executors;
    private final EventWorker eventWorker;

    /** Creates a customer queue coordinator with its required state. */
    public CustomerQueueCoordinator(
            ProcessingProperties properties,
            WorkerExecutors executors,
            EventWorker eventWorker) {
        this.readyCustomers = new LinkedBlockingQueue<>(properties.readyQueueCapacity());
        this.workerCount = properties.workerCount();
        this.executors = executors;
        this.eventWorker = eventWorker;
    }

    @PostConstruct
    /** Starts the configured number of worker loops. */
    void startWorkers() {
        for (int index = 0; index < workerCount; index++) {
            executors.workerExecutor().submit(this::workerLoop);
        }
    }

    /** Queues a customer once when its head may be eligible. */
    public void signal(CustomerKey key) {
        LaneState lane = lanes.computeIfAbsent(key, ignored -> new LaneState());
        if (!lane.scheduled.compareAndSet(false, true)) {
            return;
        }
        if (!readyCustomers.offer(key)) {
            lane.scheduled.set(false);
            schedule(key, Duration.ofMillis(100));
        }
    }

    /** Signals a customer after a non-blocking delay. */
    public void schedule(CustomerKey key, Duration delay) {
        long milliseconds = Math.max(0, delay.toMillis());
        executors.retryScheduler().schedule(() -> signal(key), milliseconds, TimeUnit.MILLISECONDS);
    }

    /** Returns the number of worker passes currently executing. */
    public int inFlightCount() {
        return inFlight.get();
    }

    /** Checks whether a customer lane currently owns a worker. */
    public boolean isRunning(CustomerKey key) {
        LaneState lane = lanes.get(key);
        return lane != null && lane.running.get();
    }

    /** Continuously consumes ready customer tokens. */
    private void workerLoop() {
        while (active.get() && !Thread.currentThread().isInterrupted()) {
            try {
                CustomerKey key = readyCustomers.take();
                runOneCustomer(key);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException unexpected) {
                System.getLogger(getClass().getName()).log(
                        System.Logger.Level.ERROR,
                        UNEXPECTED_WORKER_FAILURE,
                        unexpected);
            }
        }
    }

    /** Runs at most one eligible event for a customer token. */
    private void runOneCustomer(CustomerKey key) {
        LaneState lane = lanes.computeIfAbsent(key, ignored -> new LaneState());
        lane.scheduled.set(false);
        if (!lane.running.compareAndSet(false, true)) {
            return;
        }

        ProcessingDecision decision;
        inFlight.incrementAndGet();
        try {
            decision = eventWorker.processNext(key);
        } finally {
            inFlight.decrementAndGet();
            lane.running.set(false);
        }

        switch (decision.getType()) {
            case ADVANCE -> signal(key);
            case WAIT -> schedule(key, decision.getDelay());
            case BLOCKED, NOTHING -> { }
        }
    }

    @PreDestroy
    /** Stops worker loops during application shutdown. */
    void stopWorkers() {
        active.set(false);
        executors.workerExecutor().shutdownNow();
    }

    /** Tracks whether one customer lane is scheduled or currently running. */
    private static final class LaneState {
        private final AtomicBoolean scheduled = new AtomicBoolean();
        private final AtomicBoolean running = new AtomicBoolean();
    }
}
