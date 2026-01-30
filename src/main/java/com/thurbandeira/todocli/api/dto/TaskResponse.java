package com.thurbandeira.todocli.api.dto;

public record TaskResponse(
        int id,
        String title,
        boolean completed
) {
}
