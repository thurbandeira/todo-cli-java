package com.thurbandeira.todocli.api.application.task.usecase;

import com.thurbandeira.todocli.api.application.task.support.TaskOwnerResolver;
import com.thurbandeira.todocli.api.application.task.support.TaskTitleValidator;
import com.thurbandeira.todocli.api.domain.TaskEntity;
import com.thurbandeira.todocli.api.domain.UserAccount;
import com.thurbandeira.todocli.api.repository.TaskRepository;
import com.thurbandeira.todocli.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class CreateTaskUseCase {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CreateTaskUseCase(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TaskEntity execute(String username, String title) {
        String normalized = TaskTitleValidator.normalize(title);
        UserAccount user = TaskOwnerResolver.requireUser(username, userRepository);
        TaskEntity task = new TaskEntity(normalized, user);
        return taskRepository.save(task);
    }
}
