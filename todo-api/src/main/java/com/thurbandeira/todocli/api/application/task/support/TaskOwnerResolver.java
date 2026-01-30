package com.thurbandeira.todocli.api.application.task.support;

import com.thurbandeira.todocli.api.domain.UserAccount;
import com.thurbandeira.todocli.api.exception.NotFoundException;
import com.thurbandeira.todocli.api.repository.UserRepository;

public final class TaskOwnerResolver {

    private TaskOwnerResolver() {
    }

    public static UserAccount requireUser(String username, UserRepository userRepository) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado."));
    }
}
