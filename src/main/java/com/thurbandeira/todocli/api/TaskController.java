package com.thurbandeira.todocli.api;

import com.thurbandeira.todocli.api.dto.SummaryResponse;
import com.thurbandeira.todocli.api.dto.TaskRequest;
import com.thurbandeira.todocli.api.dto.TaskResponse;
import com.thurbandeira.todocli.api.dto.TaskUpdateRequest;
import com.thurbandeira.todocli.model.Task;
import com.thurbandeira.todocli.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskManager taskManager;

    public TaskController(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @GetMapping
    public List<TaskResponse> listTasks(@RequestParam(name = "status", required = false) String status) {
        List<Task> tasks;
        if (status == null || status.isBlank()) {
            tasks = taskManager.listAll();
        } else if ("pending".equalsIgnoreCase(status)) {
            tasks = taskManager.listByStatus(false);
        } else if ("completed".equalsIgnoreCase(status)) {
            tasks = taskManager.listByStatus(true);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status invalido");
        }
        return tasks.stream().map(this::toResponse).toList();
    }

    @GetMapping("/search")
    public List<TaskResponse> search(@RequestParam(name = "keyword") String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "palavra-chave obrigatoria");
        }
        return taskManager.searchByKeyword(keyword.trim()).stream().map(this::toResponse).toList();
    }

    @GetMapping("/summary")
    public SummaryResponse summary() {
        TaskService.Summary summary = taskManager.getSummary();
        return new SummaryResponse(summary.total(), summary.pending(), summary.done());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskRequest request) {
        try {
            Task task = taskManager.addTask(request.title());
            return toResponse(task);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public TaskResponse update(@PathVariable int id, @RequestBody TaskUpdateRequest request) {
        if ((request.title() == null || request.title().isBlank()) && request.completed() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nenhuma alteracao enviada");
        }
        String title = request.title();
        if (title != null && title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "titulo nao pode ser vazio");
        }
        try {
            return taskManager.updateTask(id, title, request.completed())
                    .map(this::toResponse)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "tarefa nao encontrada"));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        boolean removed = taskManager.removeTask(id);
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "tarefa nao encontrada");
        }
    }

    @PostMapping("/clear-completed")
    public SummaryResponse clearCompleted() {
        taskManager.clearCompleted();
        TaskService.Summary summary = taskManager.getSummary();
        return new SummaryResponse(summary.total(), summary.pending(), summary.done());
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.isCompleted());
    }
}
