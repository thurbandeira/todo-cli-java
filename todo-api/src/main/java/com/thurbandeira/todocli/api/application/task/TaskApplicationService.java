package com.thurbandeira.todocli.api.application.task;

import com.thurbandeira.todocli.api.application.task.usecase.ClearCompletedUseCase;
import com.thurbandeira.todocli.api.application.task.usecase.CompleteTaskUseCase;
import com.thurbandeira.todocli.api.application.task.usecase.CreateTaskUseCase;
import com.thurbandeira.todocli.api.application.task.usecase.ListTasksPagedUseCase;
import com.thurbandeira.todocli.api.application.task.usecase.ListTasksUseCase;
import com.thurbandeira.todocli.api.application.task.usecase.RemoveTaskUseCase;
import com.thurbandeira.todocli.api.application.task.usecase.SearchTasksPagedUseCase;
import com.thurbandeira.todocli.api.application.task.usecase.SearchTasksUseCase;
import com.thurbandeira.todocli.api.application.task.usecase.SummaryTasksUseCase;
import com.thurbandeira.todocli.api.application.task.usecase.UpdateTaskTitleUseCase;
import com.thurbandeira.todocli.api.domain.TaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskApplicationService implements TaskUseCases {

    private final ListTasksUseCase listTasksUseCase;
    private final ListTasksPagedUseCase listTasksPagedUseCase;
    private final SearchTasksUseCase searchTasksUseCase;
    private final SearchTasksPagedUseCase searchTasksPagedUseCase;
    private final CreateTaskUseCase createTaskUseCase;
    private final UpdateTaskTitleUseCase updateTaskTitleUseCase;
    private final CompleteTaskUseCase completeTaskUseCase;
    private final RemoveTaskUseCase removeTaskUseCase;
    private final SummaryTasksUseCase summaryTasksUseCase;
    private final ClearCompletedUseCase clearCompletedUseCase;

    public TaskApplicationService(ListTasksUseCase listTasksUseCase,
                                  ListTasksPagedUseCase listTasksPagedUseCase,
                                  SearchTasksUseCase searchTasksUseCase,
                                  SearchTasksPagedUseCase searchTasksPagedUseCase,
                                  CreateTaskUseCase createTaskUseCase,
                                  UpdateTaskTitleUseCase updateTaskTitleUseCase,
                                  CompleteTaskUseCase completeTaskUseCase,
                                  RemoveTaskUseCase removeTaskUseCase,
                                  SummaryTasksUseCase summaryTasksUseCase,
                                  ClearCompletedUseCase clearCompletedUseCase) {
        this.listTasksUseCase = listTasksUseCase;
        this.listTasksPagedUseCase = listTasksPagedUseCase;
        this.searchTasksUseCase = searchTasksUseCase;
        this.searchTasksPagedUseCase = searchTasksPagedUseCase;
        this.createTaskUseCase = createTaskUseCase;
        this.updateTaskTitleUseCase = updateTaskTitleUseCase;
        this.completeTaskUseCase = completeTaskUseCase;
        this.removeTaskUseCase = removeTaskUseCase;
        this.summaryTasksUseCase = summaryTasksUseCase;
        this.clearCompletedUseCase = clearCompletedUseCase;
    }

    @Override
    public List<TaskEntity> listAll(String username) {
        return listTasksUseCase.listAll(username);
    }

    @Override
    public List<TaskEntity> listByStatus(String username, boolean completed) {
        return listTasksUseCase.listByStatus(username, completed);
    }

    @Override
    public List<TaskEntity> search(String username, String keyword) {
        return searchTasksUseCase.execute(username, keyword);
    }

    @Override
    public Page<TaskEntity> listAllPaged(String username, Pageable pageable) {
        return listTasksPagedUseCase.listAll(username, pageable);
    }

    @Override
    public Page<TaskEntity> listByStatusPaged(String username, boolean completed, Pageable pageable) {
        return listTasksPagedUseCase.listByStatus(username, completed, pageable);
    }

    @Override
    public Page<TaskEntity> searchPaged(String username, String keyword, Pageable pageable) {
        return searchTasksPagedUseCase.execute(username, keyword, pageable);
    }

    @Override
    public TaskEntity add(String username, String title) {
        return createTaskUseCase.execute(username, title);
    }

    @Override
    public TaskEntity update(String username, long id, String title) {
        return updateTaskTitleUseCase.execute(username, id, title);
    }

    @Override
    public TaskEntity complete(String username, long id) {
        return completeTaskUseCase.execute(username, id);
    }

    @Override
    public void remove(String username, long id) {
        removeTaskUseCase.execute(username, id);
    }

    @Override
    public Summary summary(String username) {
        return summaryTasksUseCase.execute(username);
    }

    @Override
    public Summary clearCompleted(String username) {
        return clearCompletedUseCase.execute(username);
    }
}
