package com.thurbandeira.todocli.api.service;

import com.thurbandeira.todocli.model.Task;
import com.thurbandeira.todocli.service.TaskService;
import com.thurbandeira.todocli.storage.TaskRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskManager {

    private final TaskService taskService;
    private final TaskRepository storage;

    public TaskManager(TaskService taskService, TaskRepository storage) {
        this.taskService = taskService;
        this.storage = storage;
    }

    @PostConstruct
    void init() {
        List<Task> loadedTasks = storage.load();
        taskService.setTasks(loadedTasks);

        int maxId = 0;
        for (Task t : loadedTasks) {
            if (t.getId() > maxId) maxId = t.getId();
        }
        taskService.setNextId(maxId + 1);
    }

    public List<Task> listAll() {
        return taskService.listTasksSorted();
    }

    public List<Task> listByStatus(boolean completed) {
        return taskService.listByStatus(completed);
    }

    public List<Task> search(String keyword) {
        return taskService.searchByKeyword(keyword);
    }

    public Task addTask(String title) {
        Task task = taskService.addTask(title);
        storage.save(taskService.getTasks());
        return task;
    }

    public TaskService.UpdateResult updateTitle(int id, String title) {
        TaskService.UpdateResult result = taskService.updateTitle(id, title);
        if (result == TaskService.UpdateResult.UPDATED) {
            storage.save(taskService.getTasks());
        }
        return result;
    }

    public TaskService.MarkResult markCompleted(int id) {
        TaskService.MarkResult result = taskService.markTaskAsCompleted(id);
        if (result == TaskService.MarkResult.MARKED) {
            storage.save(taskService.getTasks());
        }
        return result;
    }

    public boolean remove(int id) {
        boolean removed = taskService.removeTask(id);
        if (removed) {
            storage.save(taskService.getTasks());
        }
        return removed;
    }

    public Task getById(int id) {
        return taskService.getById(id);
    }

    public TaskService.Summary summary() {
        return taskService.getSummary();
    }

    public TaskService.Summary clearCompleted() {
        int removed = taskService.clearCompleted();
        if (removed > 0) {
            storage.save(taskService.getTasks());
        }
        return taskService.getSummary();
    }
}
