package com.thurbandeira.todocli.api.application.task;

import com.thurbandeira.todocli.api.application.task.usecase.CreateTaskUseCase;
import com.thurbandeira.todocli.api.domain.TaskEntity;
import com.thurbandeira.todocli.api.domain.UserAccount;
import com.thurbandeira.todocli.api.exception.ValidationException;
import com.thurbandeira.todocli.api.repository.TaskRepository;
import com.thurbandeira.todocli.api.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateTaskUseCaseTest {

    @Test
    void execute_trimsTitleAndSaves() {
        TaskRepository taskRepository = mock(TaskRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CreateTaskUseCase useCase = new CreateTaskUseCase(taskRepository, userRepository);

        UserAccount user = new UserAccount("user", "hash");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(taskRepository.save(org.mockito.ArgumentMatchers.any(TaskEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskEntity task = useCase.execute("user", "  Titulo  ");

        assertEquals("Titulo", task.getTitle());
    }

    @Test
    void execute_rejectsBlankTitle() {
        TaskRepository taskRepository = mock(TaskRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CreateTaskUseCase useCase = new CreateTaskUseCase(taskRepository, userRepository);

        assertThrows(ValidationException.class, () -> useCase.execute("user", "   "));
    }
}
