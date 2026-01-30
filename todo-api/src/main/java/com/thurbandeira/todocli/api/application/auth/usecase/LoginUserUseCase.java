package com.thurbandeira.todocli.api.application.auth.usecase;

import com.thurbandeira.todocli.api.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class LoginUserUseCase {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginUserUseCase(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String execute(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        return jwtService.generateToken(username);
    }
}
