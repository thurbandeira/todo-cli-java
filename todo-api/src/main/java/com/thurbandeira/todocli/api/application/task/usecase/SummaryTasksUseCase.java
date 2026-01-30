package com.thurbandeira.todocli.api.application.task.usecase;

import com.thurbandeira.todocli.api.application.task.TaskUseCases;
import com.thurbandeira.todocli.api.application.task.support.TaskOwnerResolver;
import com.thurbandeira.todocli.api.domain.UserAccount;
import com.thurbandeira.todocli.api.repository.TaskRepository;
import com.thurbandeira.todocli.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class SummaryTasksUseCase {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public SummaryTasksUseCase(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TaskUseCases.Summary execute(String username) {
        UserAccount user = TaskOwnerResolver.requireUser(username, userRepository);
        long total = taskRepository.countByOwner(user);
        long done = taskRepository.countByOwnerAndCompleted(user, true);
        long pending = total - done;
        return new TaskUseCases.Summary((int) total, (int) pending, (int) done);
    }
}
