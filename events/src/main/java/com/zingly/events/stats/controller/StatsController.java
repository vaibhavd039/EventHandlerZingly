package com.zingly.events.stats.controller;

import static com.zingly.events.common.constants.ApiConstants.*;
import com.zingly.events.stats.dto.StatsResponse;
import com.zingly.events.stats.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = STATS_TAG, description = STATS_TAG_DESCRIPTION)
/** Exposes the processing statistics REST endpoint. */
public class StatsController {

    private final StatsService statsService;

    /** Creates a stats controller with its required state. */
    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping(STATS_PATH)
    @Operation(summary = STATS_SUMMARY, description = STATS_DESCRIPTION)
    /** Returns the current processing statistics. */
    StatsResponse stats() {
        return statsService.snapshot();
    }
}
