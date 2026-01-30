package com.thurbandeira.todocli.api.application.auth.usecase;

import com.thurbandeira.todocli.api.exception.UnauthorizedException;
import com.thurbandeira.todocli.api.repository.UserRepository;
import com.thurbandeira.todocli.api.security.JwtService;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenUseCase {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public RefreshTokenUseCase(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public String execute(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Token ausente.");
        }
        String username = jwtService.extractUsername(token);
        if (!jwtService.isTokenValid(token, username)) {
            throw new UnauthorizedException("Token invalido.");
        }
        if (userRepository.findByUsername(username).isEmpty()) {
            throw new UnauthorizedException("Usuario nao encontrado.");
        }
        return jwtService.generateToken(username);
    }
}
