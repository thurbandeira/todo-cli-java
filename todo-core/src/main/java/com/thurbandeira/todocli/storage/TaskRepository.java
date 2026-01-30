package com.thurbandeira.todocli.storage;

import com.thurbandeira.todocli.model.Task;

import java.util.List;

public interface TaskRepository {
    List<Task> load();
    void save(List<Task> tasks);
}
