package com.thurbandeira.todocli.api;

import com.thurbandeira.todocli.model.Task;
import com.thurbandeira.todocli.service.TaskService;
import com.thurbandeira.todocli.storage.TaskRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskManager {

    private final TaskService taskService;
    private final TaskRepository storage;

    public TaskManager(TaskRepository storage) {
        this.storage = storage;
        this.taskService = new TaskService();
    }

    @PostConstruct
    public void load() {
        List<Task> loadedTasks = storage.load();
        taskService.setTasks(loadedTasks);
        int maxId = 0;
        for (Task task : loadedTasks) {
            if (task.getId() > maxId) {
                maxId = task.getId();
            }
        }
        taskService.setNextId(maxId + 1);
    }

    public List<Task> listAll() {
        return taskService.listTasksSorted();
    }

    public List<Task> listByStatus(boolean completed) {
        return taskService.listByStatus(completed);
    }

    public TaskService.Summary getSummary() {
        return taskService.getSummary();
    }

    public Task addTask(String title) {
        Task task = taskService.addTask(title);
        storage.save(taskService.getTasks());
        return task;
    }

    public List<Task> searchByKeyword(String keyword) {
        return taskService.searchByKeyword(keyword);
    }

    public int clearCompleted() {
        int removed = taskService.clearCompleted();
        if (removed > 0) {
            storage.save(taskService.getTasks());
        }
        return removed;
    }

    public Optional<Task> updateTask(int id, String title, Boolean completed) {
        if (title != null) {
            TaskService.UpdateResult updateResult = taskService.updateTitle(id, title);
            if (updateResult == TaskService.UpdateResult.NOT_FOUND) {
                return Optional.empty();
            }
            if (updateResult == TaskService.UpdateResult.INVALID_TITLE) {
                throw new IllegalArgumentException("titulo invalido");
            }
        }
        if (completed != null) {
            TaskService.UpdateCompletionResult completionResult = taskService.updateCompletion(id, completed);
            if (completionResult == TaskService.UpdateCompletionResult.NOT_FOUND) {
                return Optional.empty();
            }
        }
        storage.save(taskService.getTasks());
        return taskService.getTasks().stream().filter(task -> task.getId() == id).findFirst();
    }

    public boolean removeTask(int id) {
        boolean removed = taskService.removeTask(id);
        if (removed) {
            storage.save(taskService.getTasks());
        }
        return removed;
    }
}
