package com.thurbandeira.todocli.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskRequest(
        @NotBlank(message = "Titulo obrigatorio.")
        String title
) {
}
