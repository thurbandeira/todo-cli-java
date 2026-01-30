package com.thurbandeira.todocli.api.application.task.usecase;

import com.thurbandeira.todocli.api.application.task.support.TaskOwnerResolver;
import com.thurbandeira.todocli.api.domain.TaskEntity;
import com.thurbandeira.todocli.api.domain.UserAccount;
import com.thurbandeira.todocli.api.repository.TaskRepository;
import com.thurbandeira.todocli.api.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListTasksUseCase {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public ListTasksUseCase(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<TaskEntity> listAll(String username) {
        UserAccount user = TaskOwnerResolver.requireUser(username, userRepository);
        return taskRepository.findAllByOwnerOrderByCompletedAscIdAsc(user);
    }

    public List<TaskEntity> listByStatus(String username, boolean completed) {
        UserAccount user = TaskOwnerResolver.requireUser(username, userRepository);
        return taskRepository.findAllByOwnerAndCompletedOrderByIdAsc(user, completed);
    }
}
