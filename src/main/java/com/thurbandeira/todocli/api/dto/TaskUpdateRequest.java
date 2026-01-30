package com.thurbandeira.todocli.api.dto;

public record TaskUpdateRequest(
        String title,
        Boolean completed
) {
}
