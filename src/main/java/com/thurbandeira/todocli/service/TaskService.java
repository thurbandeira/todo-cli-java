package com.thurbandeira.todocli.service;

import com.thurbandeira.todocli.model.Task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TaskService {

    private List<Task> tasks = new ArrayList<>();
    private int nextId = 1;

    public Task addTask(String title) {
        Task task = new Task(nextId, title);
        tasks.add(task);
        nextId++;
        return task;
    }

    public List<Task> listTasksSorted() {
        List<Task> sorted = new ArrayList<>(tasks);
        sorted.sort(Comparator.comparing(Task::isCompleted).thenComparingInt(Task::getId));
        return sorted;
    }

    public MarkResult markTaskAsCompleted(int id) {
        Task task = findById(id);
        if (task == null) {
            return MarkResult.NOT_FOUND;
        }

        if (task.isCompleted()) {
            return MarkResult.ALREADY_COMPLETED;
        }

        task.markAsCompleted();
        return MarkResult.MARKED;
    }

    public boolean removeTask(int id) {
        Task task = findById(id);
        if (task == null) {
            return false;
        }

        tasks.remove(task);
        return true;
    }

    public UpdateResult updateTitle(int id, String newTitle) {
        if (newTitle == null || newTitle.trim().isEmpty()) {
            return UpdateResult.INVALID_TITLE;
        }
        Task task = findById(id);
        if (task == null) {
            return UpdateResult.NOT_FOUND;
        }
        task.setTitle(newTitle.trim());
        return UpdateResult.UPDATED;
    }

    public List<Task> listByStatus(boolean completed) {
        List<Task> filtered = new ArrayList<>();
        for (Task task : tasks) {
            if (task.isCompleted() == completed) {
                filtered.add(task);
            }
        }
        filtered.sort(Comparator.comparingInt(Task::getId));
        return filtered;
    }

    private Task findById(int id) {
        for (Task task : tasks) {
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    public void setTasks(List<Task> loadedTasks) {
        this.tasks = loadedTasks;
    }

    public int clearCompleted() {
        int before = tasks.size();
        tasks.removeIf(Task::isCompleted);
        int removed = before - tasks.size();
        return removed;
    }

    public List<Task> searchByKeyword(String keyword) {
        String k = keyword.toLowerCase();
        List<Task> results = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getTitle().toLowerCase().contains(k)) {
                results.add(task);
            }
        }
        return results;
    }

    public Summary getSummary() {
        int done = 0;
        for (Task task : tasks) {
            if (task.isCompleted()) done++;
        }
        int total = tasks.size();
        int pending = total - done;
        return new Summary(total, pending, done);
    }

    public enum MarkResult {
        MARKED,
        ALREADY_COMPLETED,
        NOT_FOUND
    }

    public enum UpdateResult {
        UPDATED,
        NOT_FOUND,
        INVALID_TITLE
    }

    public record Summary(int total, int pending, int done) {
    }

}
