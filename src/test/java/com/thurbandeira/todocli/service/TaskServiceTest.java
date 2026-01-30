package com.thurbandeira.todocli.service;

import com.thurbandeira.todocli.model.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {

    @Test
    void addTask_assignsIncrementalIds() {
        TaskService service = new TaskService();

        service.addTask("Primeira");
        service.addTask("Segunda");

        List<Task> tasks = service.getTasks();
        assertEquals(2, tasks.size());
        assertEquals(1, tasks.get(0).getId());
        assertEquals(2, tasks.get(1).getId());
    }

    @Test
    void markTaskAsCompleted_handlesMissingAndAlreadyDone() {
        TaskService service = new TaskService();
        service.addTask("Tarefa");

        TaskService.MarkResult firstMark = service.markTaskAsCompleted(1);
        TaskService.MarkResult secondMark = service.markTaskAsCompleted(1);
        TaskService.MarkResult missing = service.markTaskAsCompleted(999);

        assertEquals(TaskService.MarkResult.MARKED, firstMark);
        assertEquals(TaskService.MarkResult.ALREADY_COMPLETED, secondMark);
        assertEquals(TaskService.MarkResult.NOT_FOUND, missing);
        assertTrue(service.getTasks().get(0).isCompleted());
    }

    @Test
    void clearCompleted_removesOnlyCompletedTasks() {
        TaskService service = new TaskService();
        service.addTask("A");
        service.addTask("B");
        service.addTask("C");
        service.markTaskAsCompleted(2);

        int removed = service.clearCompleted();

        assertEquals(1, removed);
        assertEquals(2, service.getTasks().size());
        assertTrue(service.getTasks().stream().noneMatch(t -> t.getId() == 2));
    }
}
