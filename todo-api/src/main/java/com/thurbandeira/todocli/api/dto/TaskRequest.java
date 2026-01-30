package com.thurbandeira.todocli.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotBlank(message = "Titulo obrigatorio.")
        @Size(max = 200, message = "Titulo deve ter no maximo 200 caracteres.")
        String title
) {
}
