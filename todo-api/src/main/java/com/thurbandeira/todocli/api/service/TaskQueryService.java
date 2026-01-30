package com.thurbandeira.todocli.api.service;

import com.thurbandeira.todocli.api.domain.TaskEntity;
import com.thurbandeira.todocli.api.domain.UserAccount;
import com.thurbandeira.todocli.api.exception.NotFoundException;
import com.thurbandeira.todocli.api.exception.ValidationException;
import com.thurbandeira.todocli.api.repository.TaskRepository;
import com.thurbandeira.todocli.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskQueryService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskQueryService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<TaskEntity> listAll(String username) {
        UserAccount user = requireUser(username);
        return taskRepository.findAllByOwnerOrderByCompletedAscIdAsc(user);
    }

    public List<TaskEntity> listByStatus(String username, boolean completed) {
        UserAccount user = requireUser(username);
        return taskRepository.findAllByOwnerAndCompletedOrderByIdAsc(user, completed);
    }

    public List<TaskEntity> search(String username, String keyword) {
        UserAccount user = requireUser(username);
        return taskRepository.findAllByOwnerAndTitleContainingIgnoreCaseOrderByIdAsc(user, keyword);
    }

    @Transactional
    public TaskEntity add(String username, String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Titulo invalido.");
        }
        UserAccount user = requireUser(username);
        TaskEntity task = new TaskEntity(title.trim(), user);
        return taskRepository.save(task);
    }

    @Transactional
    public TaskEntity update(String username, long id, String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Titulo invalido.");
        }
        UserAccount user = requireUser(username);
        TaskEntity task = taskRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new NotFoundException("Tarefa nao encontrada."));
        task.setTitle(title.trim());
        return task;
    }

    @Transactional
    public TaskEntity complete(String username, long id) {
        UserAccount user = requireUser(username);
        TaskEntity task = taskRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new NotFoundException("Tarefa nao encontrada."));
        task.markCompleted();
        return task;
    }

    @Transactional
    public void remove(String username, long id) {
        UserAccount user = requireUser(username);
        TaskEntity task = taskRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new NotFoundException("Tarefa nao encontrada."));
        taskRepository.delete(task);
    }

    @Transactional
    public Summary summary(String username) {
        UserAccount user = requireUser(username);
        long total = taskRepository.countByOwner(user);
        long done = taskRepository.countByOwnerAndCompleted(user, true);
        long pending = total - done;
        return new Summary((int) total, (int) pending, (int) done);
    }

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

    public record Summary(int total, int pending, int done) {
    }
}
