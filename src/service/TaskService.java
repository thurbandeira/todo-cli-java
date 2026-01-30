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

}