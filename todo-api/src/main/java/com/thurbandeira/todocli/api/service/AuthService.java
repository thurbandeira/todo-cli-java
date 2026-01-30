package com.thurbandeira.todocli.api.service;

import com.thurbandeira.todocli.api.domain.UserAccount;
import com.thurbandeira.todocli.api.exception.UnauthorizedException;
import com.thurbandeira.todocli.api.exception.ValidationException;
import com.thurbandeira.todocli.api.repository.UserRepository;
import com.thurbandeira.todocli.api.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new ValidationException("Usuario ja existe.");
        }
        UserAccount user = new UserAccount(username, passwordEncoder.encode(password));
        userRepository.save(user);
        return jwtService.generateToken(user.getUsername());
    }

    public String login(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        return jwtService.generateToken(username);
    }

    public String refresh(String token) {
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
