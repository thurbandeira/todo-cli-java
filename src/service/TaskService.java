package service;

import model.Task;
import java.io.*;
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

        tasks.stream()
                .sorted((a, b) -> Boolean.compare(a.isCompleted(), b.isCompleted()))
                .forEach(task -> {

                    String status = task.isCompleted() ? "[X]" : "[ ]";
                    String doneText = task.isCompleted() ? " ✔ - Concluída" : "";

                    System.out.println(
                            status + " " + task.getId() + " - " + task.getTitle() + doneText
                    );
                });

        showSummary();
    }

    public boolean markTaskAsCompleted(int id) {
        Task task = findById(id);
        if (task == null) {
            System.out.println("Não existe tarefa com esse ID.");
            return false;
        }

        if (task.isCompleted()) {
            System.out.println("Essa tarefa já está concluída.");
            return false;
        }

        task.markAsCompleted();
        System.out.println("Tarefa marcada como concluída!");
        return true;
    }

    public boolean removeTask(int id) {
        Task task = findById(id);
        if (task == null) {
            System.out.println("Não existe tarefa com esse ID.");
            return false;
        }

        tasks.remove(task);
        System.out.println("Tarefa removida com sucesso!");
        return true;
    }

    private Task findById(int id) {
        for (Task task : tasks) {
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    public void setTasks(List<Task> loadedTasks) {
        this.tasks = loadedTasks;
    }

    public int clearCompleted() {
        int before = tasks.size();
        tasks.removeIf(Task::isCompleted);
        int removed = before - tasks.size();

        if (removed == 0) {
            System.out.println("Nenhuma tarefa concluída para remover.");
        } else {
            System.out.println("Removidas " + removed + " tarefa(s) concluída(s).");
        }
        return removed;
    }

    public void searchByKeyword(String keyword) {
        if (tasks.isEmpty()) {
            System.out.println("Nenhuma tarefa cadastrada.");
            return;
        }

        String k = keyword.toLowerCase();
        boolean found = false;

        System.out.println("\n--- RESULTADO DA BUSCA ---");
        for (Task task : tasks) {
            if (task.getTitle().toLowerCase().contains(k)) {
                String status = task.isCompleted() ? "[X]" : "[ ]";
                System.out.println(status + " " + task.getId() + " - " + task.getTitle());
                found = true;
            }
        }

        if (!found) {
            System.out.println("Nenhuma tarefa encontrada para: " + keyword);
        }
    }

    public void showSummary() {
        int done = 0;
        for (Task task : tasks) {
            if (task.isCompleted()) done++;
        }
        int total = tasks.size();
        int pending = total - done;

        System.out.println("\nResumo: " + total + " total | " + pending + " pendentes | " + done + " concluídas");
    }


}