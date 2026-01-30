package com.thurbandeira.todocli.api.dto;

public record TaskResponse(
        long id,
        String title,
        boolean completed
) {
}
