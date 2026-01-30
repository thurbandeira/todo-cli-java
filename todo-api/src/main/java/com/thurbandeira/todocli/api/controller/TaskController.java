package com.thurbandeira.todocli.api.controller;

import com.thurbandeira.todocli.api.dto.SummaryResponse;
import com.thurbandeira.todocli.api.dto.TaskRequest;
import com.thurbandeira.todocli.api.dto.TaskResponse;
import com.thurbandeira.todocli.api.dto.TaskUpdateRequest;
import com.thurbandeira.todocli.api.service.TaskManager;
import com.thurbandeira.todocli.model.Task;
import com.thurbandeira.todocli.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Validated
public class TaskController {

    private final TaskManager manager;

    public TaskController(TaskManager manager) {
        this.manager = manager;
    }

    @GetMapping
    public List<TaskResponse> list(@RequestParam(defaultValue = "all") String status) {
        return switch (normalizeStatus(status)) {
            case "all" -> mapTasks(manager.listAll());
            case "pending" -> mapTasks(manager.listByStatus(false));
            case "completed" -> mapTasks(manager.listByStatus(true));
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status invalido.");
        };
    }

    @GetMapping("/summary")
    public SummaryResponse summary() {
        TaskService.Summary summary = manager.summary();
        return new SummaryResponse(summary.total(), summary.pending(), summary.done());
    }

    @GetMapping("/search")
    public List<TaskResponse> search(@RequestParam @NotBlank(message = "Keyword obrigatoria.") String keyword) {
        return mapTasks(manager.search(keyword));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        Task task = manager.addTask(request.title());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapTask(task));
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable int id, @Valid @RequestBody TaskUpdateRequest request) {
        TaskService.UpdateResult result = manager.updateTitle(id, request.title());
        if (result == TaskService.UpdateResult.NOT_FOUND) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa nao encontrada.");
        }
        if (result == TaskService.UpdateResult.INVALID_TITLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Titulo invalido.");
        }
        Task updated = manager.getById(id);
        return mapTask(updated);
    }

    @PostMapping("/{id}/complete")
    public TaskResponse complete(@PathVariable int id) {
        TaskService.MarkResult result = manager.markCompleted(id);
        if (result == TaskService.MarkResult.NOT_FOUND) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa nao encontrada.");
        }
        Task updated = manager.getById(id);
        return mapTask(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable int id) {
        if (!manager.remove(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa nao encontrada.");
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/clear-completed")
    public SummaryResponse clearCompleted() {
        TaskService.Summary summary = manager.clearCompleted();
        return new SummaryResponse(summary.total(), summary.pending(), summary.done());
    }

    private List<TaskResponse> mapTasks(List<Task> tasks) {
        return tasks.stream().map(this::mapTask).toList();
    }

    private TaskResponse mapTask(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.isCompleted());
    }

    private String normalizeStatus(String status) {
        if (status == null) return "all";
        return status.trim().toLowerCase();
    }
}
