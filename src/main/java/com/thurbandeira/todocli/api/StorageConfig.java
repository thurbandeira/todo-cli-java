package com.thurbandeira.todocli.api;

import com.thurbandeira.todocli.storage.Storage;
import com.thurbandeira.todocli.storage.TaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    public TaskRepository taskRepository(@Value("${todo.storage.path:data/tasks.json}") String path) {
        return new Storage(path);
    }
}
