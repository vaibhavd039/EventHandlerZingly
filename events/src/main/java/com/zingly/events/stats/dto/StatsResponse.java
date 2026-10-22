package com.zingly.events.stats.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/** Carries stats data returned by the API. */
public class StatsResponse {

    private long processed;
    private long inFlight;
    private long retried;
    private long deadLettered;
    private long dlqSize;
    private long queued;
    private long retryWaiting;
    private long blockedCustomers;
    private long recoveryPending;
    private int windowSeconds;
    private Instant snapshotAt;
    private List<ChannelStatsResponse> perChannel;
}
