package com.thurbandeira.todocli.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Usuario obrigatorio.")
        @Size(min = 3, max = 50, message = "Usuario deve ter entre 3 e 50 caracteres.")
        String username,
        @NotBlank(message = "Senha obrigatoria.")
        @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres.")
        String password
) {
}
