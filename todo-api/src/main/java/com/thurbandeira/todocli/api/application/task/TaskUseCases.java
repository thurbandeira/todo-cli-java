package com.thurbandeira.todocli.api.application.task;

import com.thurbandeira.todocli.api.domain.TaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskUseCases {
    List<TaskEntity> listAll(String username);
    List<TaskEntity> listByStatus(String username, boolean completed);
    List<TaskEntity> search(String username, String keyword);
    Page<TaskEntity> listAllPaged(String username, Pageable pageable);
    Page<TaskEntity> listByStatusPaged(String username, boolean completed, Pageable pageable);
    Page<TaskEntity> searchPaged(String username, String keyword, Pageable pageable);
    TaskEntity add(String username, String title);
    TaskEntity update(String username, long id, String title);
    TaskEntity complete(String username, long id);
    void remove(String username, long id);
    Summary summary(String username);
    Summary clearCompleted(String username);

    record Summary(int total, int pending, int done) {
    }
}
