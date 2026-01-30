package com.thurbandeira.todocli.api.controller;

import com.thurbandeira.todocli.api.dto.PageResponse;
import com.thurbandeira.todocli.api.dto.SummaryResponse;
import com.thurbandeira.todocli.api.dto.TaskRequest;
import com.thurbandeira.todocli.api.dto.TaskResponse;
import com.thurbandeira.todocli.api.dto.TaskUpdateRequest;
import com.thurbandeira.todocli.api.application.task.TaskUseCases;
import com.thurbandeira.todocli.api.domain.TaskEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Validated
public class TaskController {

    private final TaskUseCases service;

    public TaskController(TaskUseCases service) {
        this.service = service;
    }

    @GetMapping
    public List<TaskResponse> list(@AuthenticationPrincipal UserDetails user,
                                   @RequestParam(name = "status", defaultValue = "all") String status) {
        return mapTasks(listPaged(user, status, 0, 1000, "completed,asc;id,asc").getContent());
    }

    @GetMapping("/page")
    public PageResponse<TaskResponse> listPage(@AuthenticationPrincipal UserDetails user,
                                               @RequestParam(name = "status", defaultValue = "all") String status,
                                               @RequestParam(name = "page", defaultValue = "0") int page,
                                               @RequestParam(name = "size", defaultValue = "20") int size,
                                               @RequestParam(name = "sort", defaultValue = "completed,asc;id,asc") String sort) {
        Page<TaskEntity> result = listPaged(user, status, page, size, sort);
        return toPageResponse(result.map(this::mapTask));
    }

    @GetMapping("/summary")
    public SummaryResponse summary(@AuthenticationPrincipal UserDetails user) {
        TaskUseCases.Summary summary = service.summary(user.getUsername());
        return new SummaryResponse(summary.total(), summary.pending(), summary.done());
    }

    @GetMapping("/search")
    public List<TaskResponse> search(@AuthenticationPrincipal UserDetails user,
                                     @RequestParam(name = "keyword") @NotBlank(message = "Keyword obrigatoria.") String keyword) {
        return mapTasks(searchPaged(user, keyword, 0, 1000, "id,asc").getContent());
    }

    @GetMapping("/search/page")
    public PageResponse<TaskResponse> searchPage(@AuthenticationPrincipal UserDetails user,
                                                 @RequestParam(name = "keyword") @NotBlank(message = "Keyword obrigatoria.") String keyword,
                                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                                 @RequestParam(name = "size", defaultValue = "20") int size,
                                                 @RequestParam(name = "sort", defaultValue = "id,asc") String sort) {
        Page<TaskEntity> result = searchPaged(user, keyword, page, size, sort);
        return toPageResponse(result.map(this::mapTask));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@AuthenticationPrincipal UserDetails user,
                                               @Valid @RequestBody TaskRequest request) {
        TaskEntity task = service.add(user.getUsername(), request.title());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapTask(task));
    }

    @PutMapping("/{id}")
    public TaskResponse update(@AuthenticationPrincipal UserDetails user,
                               @PathVariable long id,
                               @Valid @RequestBody TaskUpdateRequest request) {
        TaskEntity updated = service.update(user.getUsername(), id, request.title());
        return mapTask(updated);
    }

    @PostMapping("/{id}/complete")
    public TaskResponse complete(@AuthenticationPrincipal UserDetails user,
                                 @PathVariable long id) {
        TaskEntity updated = service.complete(user.getUsername(), id);
        return mapTask(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal UserDetails user,
                                       @PathVariable long id) {
        service.remove(user.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/clear-completed")
    public SummaryResponse clearCompleted(@AuthenticationPrincipal UserDetails user) {
        TaskUseCases.Summary summary = service.clearCompleted(user.getUsername());
        return new SummaryResponse(summary.total(), summary.pending(), summary.done());
    }

    private List<TaskResponse> mapTasks(List<TaskEntity> tasks) {
        return tasks.stream().map(this::mapTask).toList();
    }

    private TaskResponse mapTask(TaskEntity task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.isCompleted());
    }

    private String normalizeStatus(String status) {
        if (status == null) return "all";
        return status.trim().toLowerCase();
    }

    private Page<TaskEntity> listPaged(UserDetails user, String status, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        return switch (normalizeStatus(status)) {
            case "all" -> service.listAllPaged(user.getUsername(), pageable);
            case "pending" -> service.listByStatusPaged(user.getUsername(), false, pageable);
            case "completed" -> service.listByStatusPaged(user.getUsername(), true, pageable);
            default -> throw new IllegalArgumentException("Status invalido.");
        };
    }

    private Page<TaskEntity> searchPaged(UserDetails user, String keyword, int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        return service.searchPaged(user.getUsername(), keyword, pageable);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "id");
        }
        String[] parts = sort.split(";");
        Sort result = Sort.unsorted();
        for (String part : parts) {
            String[] tokens = part.trim().split(",");
            String field = tokens[0].trim();
            Sort.Direction direction = Sort.Direction.ASC;
            if (tokens.length > 1 && "desc".equalsIgnoreCase(tokens[1].trim())) {
                direction = Sort.Direction.DESC;
            }
            result = result.and(Sort.by(direction, field));
        }
        return result;
    }

    private <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
