package com.thurbandeira.todocli.api.application.task;

import com.thurbandeira.todocli.api.application.task.usecase.UpdateTaskTitleUseCase;
import com.thurbandeira.todocli.api.domain.TaskEntity;
import com.thurbandeira.todocli.api.domain.UserAccount;
import com.thurbandeira.todocli.api.exception.NotFoundException;
import com.thurbandeira.todocli.api.repository.TaskRepository;
import com.thurbandeira.todocli.api.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateTaskTitleUseCaseTest {

    @Test
    void execute_updatesTitle() {
        TaskRepository taskRepository = mock(TaskRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        UpdateTaskTitleUseCase useCase = new UpdateTaskTitleUseCase(taskRepository, userRepository);

        UserAccount user = new UserAccount("user", "hash");
        TaskEntity task = new TaskEntity("Old", user);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(task));

        TaskEntity updated = useCase.execute("user", 1L, "Novo");

        assertEquals("Novo", updated.getTitle());
    }

    @Test
    void execute_throwsWhenNotFound() {
        TaskRepository taskRepository = mock(TaskRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        UpdateTaskTitleUseCase useCase = new UpdateTaskTitleUseCase(taskRepository, userRepository);

        UserAccount user = new UserAccount("user", "hash");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> useCase.execute("user", 1L, "Novo"));
    }
}
