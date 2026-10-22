package com.zingly.events.stats.dto;

import com.zingly.events.event.domain.enums.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/** Carries channel stats data returned by the API. */
public class ChannelStatsResponse {

    private Channel channel;
    private long processed;
    private long completedInWindow;
    private double successfulPerSecond;
}
