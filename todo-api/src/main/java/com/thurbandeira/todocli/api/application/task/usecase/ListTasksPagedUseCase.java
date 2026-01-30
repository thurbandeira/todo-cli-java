package com.thurbandeira.todocli.api.application.task.usecase;

import com.thurbandeira.todocli.api.application.task.support.TaskOwnerResolver;
import com.thurbandeira.todocli.api.domain.TaskEntity;
import com.thurbandeira.todocli.api.domain.UserAccount;
import com.thurbandeira.todocli.api.repository.TaskRepository;
import com.thurbandeira.todocli.api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ListTasksPagedUseCase {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public ListTasksPagedUseCase(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public Page<TaskEntity> listAll(String username, Pageable pageable) {
        UserAccount user = TaskOwnerResolver.requireUser(username, userRepository);
        return taskRepository.findAllByOwner(user, pageable);
    }

    public Page<TaskEntity> listByStatus(String username, boolean completed, Pageable pageable) {
        UserAccount user = TaskOwnerResolver.requireUser(username, userRepository);
        return taskRepository.findAllByOwnerAndCompleted(user, completed, pageable);
    }
}
