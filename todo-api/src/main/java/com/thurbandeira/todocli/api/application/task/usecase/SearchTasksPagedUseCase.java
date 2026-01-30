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
public class SearchTasksPagedUseCase {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public SearchTasksPagedUseCase(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public Page<TaskEntity> execute(String username, String keyword, Pageable pageable) {
        UserAccount user = TaskOwnerResolver.requireUser(username, userRepository);
        return taskRepository.findAllByOwnerAndTitleContainingIgnoreCase(user, keyword, pageable);
    }
}
