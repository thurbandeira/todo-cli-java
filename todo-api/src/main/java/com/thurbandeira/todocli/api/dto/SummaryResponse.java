package com.thurbandeira.todocli.api.dto;

public record SummaryResponse(
        int total,
        int pending,
        int done
) {
}
