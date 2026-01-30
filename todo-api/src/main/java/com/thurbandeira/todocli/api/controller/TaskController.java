package com.thurbandeira.todocli.api.controller;

import com.thurbandeira.todocli.api.dto.SummaryResponse;
import com.thurbandeira.todocli.api.dto.TaskRequest;
import com.thurbandeira.todocli.api.dto.TaskResponse;
import com.thurbandeira.todocli.api.dto.TaskUpdateRequest;
import com.thurbandeira.todocli.api.domain.TaskEntity;
import com.thurbandeira.todocli.api.service.TaskQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Validated
public class TaskController {

    private final TaskQueryService service;

    public TaskController(TaskQueryService service) {
        this.service = service;
    }

    @GetMapping
    public List<TaskResponse> list(@AuthenticationPrincipal UserDetails user,
                                   @RequestParam(name = "status", defaultValue = "all") String status) {
        return switch (normalizeStatus(status)) {
            case "all" -> mapTasks(service.listAll(user.getUsername()));
            case "pending" -> mapTasks(service.listByStatus(user.getUsername(), false));
            case "completed" -> mapTasks(service.listByStatus(user.getUsername(), true));
            default -> throw new IllegalArgumentException("Status invalido.");
        };
    }

    @GetMapping("/summary")
    public SummaryResponse summary(@AuthenticationPrincipal UserDetails user) {
        TaskQueryService.Summary summary = service.summary(user.getUsername());
        return new SummaryResponse(summary.total(), summary.pending(), summary.done());
    }

    @GetMapping("/search")
    public List<TaskResponse> search(@AuthenticationPrincipal UserDetails user,
                                     @RequestParam(name = "keyword") @NotBlank(message = "Keyword obrigatoria.") String keyword) {
        return mapTasks(service.search(user.getUsername(), keyword));
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
        TaskQueryService.Summary summary = service.clearCompleted(user.getUsername());
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
}
