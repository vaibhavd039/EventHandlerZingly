package com.zingly.events.dlq.controller;

import static com.zingly.events.common.constants.ApiConstants.*;
import java.util.UUID;
import com.zingly.events.dlq.controller.dto.DeadLetterPageResponse;
import com.zingly.events.dlq.controller.dto.ReplayResponse;
import com.zingly.events.dlq.service.DeadLetterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DLQ_PATH)
@Tag(name = DLQ_TAG, description = DLQ_TAG_DESCRIPTION)
/** Exposes REST endpoints for DLQ inspection and replay. */
public class DeadLetterController {

    private final DeadLetterService deadLetterService;

    /** Creates a dead letter controller with its required state. */
    public DeadLetterController(DeadLetterService deadLetterService) {
        this.deadLetterService = deadLetterService;
    }

    @GetMapping
    @Operation(summary = LIST_DLQ_SUMMARY, description = LIST_DLQ_DESCRIPTION)
    /** Lists dead-letter records for inspection. */
    DeadLetterPageResponse list(
            @RequestParam(defaultValue = DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = DEFAULT_FALSE) boolean includeReplayed) {
        return deadLetterService.list(page, size, includeReplayed);
    }

    @PostMapping(DLQ_REPLAY_PATH)
    @Operation(summary = REPLAY_DLQ_SUMMARY, description = REPLAY_DLQ_DESCRIPTION)
    /** Reactivates an eligible dead-lettered customer head. */
    ResponseEntity<ReplayResponse> replay(@PathVariable UUID dlqId) {
        ReplayResponse response = deadLetterService.replay(dlqId);
        return ResponseEntity.status(response.isAlreadyReplayed() ? HttpStatus.OK : HttpStatus.ACCEPTED)
                .body(response);
    }
}
