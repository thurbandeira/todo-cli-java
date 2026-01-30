package com.thurbandeira.todocli.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskUpdateRequest(
        @NotBlank(message = "Titulo obrigatorio.")
        String title
) {
}
