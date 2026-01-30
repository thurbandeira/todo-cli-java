package com.thurbandeira.todocli.api.application.task.usecase;

import com.thurbandeira.todocli.api.application.task.TaskUseCases;
import com.thurbandeira.todocli.api.application.task.support.TaskOwnerResolver;
import com.thurbandeira.todocli.api.domain.UserAccount;
import com.thurbandeira.todocli.api.repository.TaskRepository;
import com.thurbandeira.todocli.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class ClearCompletedUseCase {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SummaryTasksUseCase summaryTasksUseCase;

    public ClearCompletedUseCase(TaskRepository taskRepository,
                                 UserRepository userRepository,
                                 SummaryTasksUseCase summaryTasksUseCase) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.summaryTasksUseCase = summaryTasksUseCase;
    }

    @Transactional
    public TaskUseCases.Summary execute(String username) {
        UserAccount user = TaskOwnerResolver.requireUser(username, userRepository);
        taskRepository.deleteByOwnerAndCompleted(user, true);
        return summaryTasksUseCase.execute(username);
    }
}
