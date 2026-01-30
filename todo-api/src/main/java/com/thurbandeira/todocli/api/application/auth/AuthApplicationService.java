package com.thurbandeira.todocli.api.application.auth;

import com.thurbandeira.todocli.api.application.auth.usecase.LoginUserUseCase;
import com.thurbandeira.todocli.api.application.auth.usecase.RefreshTokenUseCase;
import com.thurbandeira.todocli.api.application.auth.usecase.RegisterUserUseCase;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService implements AuthUseCases {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthApplicationService(RegisterUserUseCase registerUserUseCase,
                                  LoginUserUseCase loginUserUseCase,
                                  RefreshTokenUseCase refreshTokenUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @Override
    public String register(String username, String password) {
        return registerUserUseCase.execute(username, password);
    }

    @Override
    public String login(String username, String password) {
        return loginUserUseCase.execute(username, password);
    }

    @Override
    public String refresh(String token) {
        return refreshTokenUseCase.execute(token);
    }
}
