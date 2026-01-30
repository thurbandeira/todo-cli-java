package com.thurbandeira.todocli.api.mapper;

import com.thurbandeira.todocli.api.domain.TaskEntity;
import com.thurbandeira.todocli.api.dto.PageResponse;
import com.thurbandeira.todocli.api.dto.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskMapper {

    public TaskResponse toResponse(TaskEntity task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.isCompleted());
    }

    public List<TaskResponse> toResponseList(List<TaskEntity> tasks) {
        return tasks.stream().map(this::toResponse).toList();
    }

    public PageResponse<TaskResponse> toPageResponse(Page<TaskEntity> page) {
        List<TaskResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
