package com.thurbandeira.todocli.api.application.auth.usecase;

import com.thurbandeira.todocli.api.domain.UserAccount;
import com.thurbandeira.todocli.api.exception.ValidationException;
import com.thurbandeira.todocli.api.repository.UserRepository;
import com.thurbandeira.todocli.api.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegisterUserUseCase(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public String execute(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new ValidationException("Usuario ja existe.");
        }
        UserAccount user = new UserAccount(username, passwordEncoder.encode(password));
        userRepository.save(user);
        return jwtService.generateToken(user.getUsername());
    }
}
