package com.thurbandeira.todocli.api.dto;

public record AuthResponse(
        String token,
        String tokenType
) {
}
