package service;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskService {

    private List<Task> tasks = new ArrayList<>();
    private int nextId = 1;

    public void addTask(String title) {
        Task task = new Task(nextId, title);
        tasks.add(task);
        nextId++;

        System.out.println("Tarefa adicionada com sucesso!");
    }

    public void listTasks() {
        if (tasks.isEmpty()) {
            System.out.println("Nenhuma tarefa cadastrada.");
            return;
        }

        System.out.println("\n--- LISTA DE TAREFAS ---");

        for (Task task : tasks) {
            String status = task.isCompleted() ? "[X]" : "[ ]";
            System.out.println(
                    status + " " + task.getId() + " - " + task.getTitle()
            );
        }
    }
}