package com.thurbandeira.todocli.api.config;

import com.thurbandeira.todocli.service.TaskService;
import com.thurbandeira.todocli.storage.Storage;
import com.thurbandeira.todocli.storage.TaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    public TaskRepository taskRepository(@Value("${storage.file-path:data/tasks.json}") String filePath) {
        return new Storage(filePath);
    }

    @Bean
    public TaskService taskService() {
        return new TaskService();
    }
}
