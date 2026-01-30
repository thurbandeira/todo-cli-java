package com.thurbandeira.todocli.api.application.auth;

public interface AuthUseCases {
    String register(String username, String password);
    String login(String username, String password);
    String refresh(String token);
}
