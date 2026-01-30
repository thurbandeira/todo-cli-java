package com.thurbandeira.todocli.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskRequest(
        @NotBlank(message = "titulo nao pode ser vazio")
        String title
) {
}
