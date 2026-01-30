package com.thurbandeira.todocli.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "Usuario obrigatorio.")
        String username,
        @NotBlank(message = "Senha obrigatoria.")
        String password
) {
}
