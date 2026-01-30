package com.thurbandeira.todocli.api.application.task;

import com.thurbandeira.todocli.api.domain.TaskEntity;
import com.thurbandeira.todocli.api.domain.UserAccount;
import com.thurbandeira.todocli.api.exception.NotFoundException;
import com.thurbandeira.todocli.api.exception.ValidationException;
import com.thurbandeira.todocli.api.repository.TaskRepository;
import com.thurbandeira.todocli.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskApplicationService implements TaskUseCases {

    private static final int MAX_TITLE = 200;

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskApplicationService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<TaskEntity> listAll(String username) {
        UserAccount user = requireUser(username);
        return taskRepository.findAllByOwnerOrderByCompletedAscIdAsc(user);
    }

    @Override
    public List<TaskEntity> listByStatus(String username, boolean completed) {
        UserAccount user = requireUser(username);
        return taskRepository.findAllByOwnerAndCompletedOrderByIdAsc(user, completed);
    }

    @Override
    public List<TaskEntity> search(String username, String keyword) {
        UserAccount user = requireUser(username);
        return taskRepository.findAllByOwnerAndTitleContainingIgnoreCaseOrderByIdAsc(user, keyword);
    }

    @Override
    public Page<TaskEntity> listAllPaged(String username, Pageable pageable) {
        UserAccount user = requireUser(username);
        return taskRepository.findAllByOwner(user, pageable);
    }

    @Override
    public Page<TaskEntity> listByStatusPaged(String username, boolean completed, Pageable pageable) {
        UserAccount user = requireUser(username);
        return taskRepository.findAllByOwnerAndCompleted(user, completed, pageable);
    }

    @Override
    public Page<TaskEntity> searchPaged(String username, String keyword, Pageable pageable) {
        UserAccount user = requireUser(username);
        return taskRepository.findAllByOwnerAndTitleContainingIgnoreCase(user, keyword, pageable);
    }

    @Override
    @Transactional
    public TaskEntity add(String username, String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Titulo invalido.");
        }
        if (title.trim().length() > MAX_TITLE) {
            throw new ValidationException("Titulo deve ter no maximo 200 caracteres.");
        }
        UserAccount user = requireUser(username);
        TaskEntity task = new TaskEntity(title.trim(), user);
        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public TaskEntity update(String username, long id, String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Titulo invalido.");
        }
        if (title.trim().length() > MAX_TITLE) {
            throw new ValidationException("Titulo deve ter no maximo 200 caracteres.");
        }
        UserAccount user = requireUser(username);
        TaskEntity task = taskRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new NotFoundException("Tarefa nao encontrada."));
        task.setTitle(title.trim());
        return task;
    }

    @Override
    @Transactional
    public TaskEntity complete(String username, long id) {
        UserAccount user = requireUser(username);
        TaskEntity task = taskRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new NotFoundException("Tarefa nao encontrada."));
        task.markCompleted();
        return task;
    }

    @Override
    @Transactional
    public void remove(String username, long id) {
        UserAccount user = requireUser(username);
        TaskEntity task = taskRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new NotFoundException("Tarefa nao encontrada."));
        taskRepository.delete(task);
    }

    @Override
    @Transactional
    public Summary summary(String username) {
        UserAccount user = requireUser(username);
        long total = taskRepository.countByOwner(user);
        long done = taskRepository.countByOwnerAndCompleted(user, true);
        long pending = total - done;
        return new Summary((int) total, (int) pending, (int) done);
    }

    @Override
    @Transactional
    public Summary clearCompleted(String username) {
        UserAccount user = requireUser(username);
        taskRepository.deleteByOwnerAndCompleted(user, true);
        return summary(username);
    }

    private UserAccount requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario nao encontrado."));
    }

}
