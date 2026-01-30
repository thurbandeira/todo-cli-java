package com.thurbandeira.todocli.api.controller;

import com.thurbandeira.todocli.api.dto.AuthRequest;
import com.thurbandeira.todocli.api.dto.AuthResponse;
import com.thurbandeira.todocli.api.dto.RegisterRequest;
import com.thurbandeira.todocli.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        String token = authService.register(request.username(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, "Bearer"));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        String token = authService.login(request.username(), request.password());
        return new AuthResponse(token, "Bearer");
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        String token = "";
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        String newToken = authService.refresh(token);
        return new AuthResponse(newToken, "Bearer");
    }
}
