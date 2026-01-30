package com.thurbandeira.todocli.api.application.task.usecase;

import com.thurbandeira.todocli.api.application.task.support.TaskOwnerResolver;
import com.thurbandeira.todocli.api.domain.TaskEntity;
import com.thurbandeira.todocli.api.domain.UserAccount;
import com.thurbandeira.todocli.api.repository.TaskRepository;
import com.thurbandeira.todocli.api.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchTasksUseCase {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public SearchTasksUseCase(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<TaskEntity> execute(String username, String keyword) {
        UserAccount user = TaskOwnerResolver.requireUser(username, userRepository);
        return taskRepository.findAllByOwnerAndTitleContainingIgnoreCaseOrderByIdAsc(user, keyword);
    }
}
