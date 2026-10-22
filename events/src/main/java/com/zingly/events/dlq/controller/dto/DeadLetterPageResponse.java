package com.zingly.events.dlq.controller.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/** Carries dead letter page data returned by the API. */
public class DeadLetterPageResponse {

    private List<DeadLetterResponse> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
