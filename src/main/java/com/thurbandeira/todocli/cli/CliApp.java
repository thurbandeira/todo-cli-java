package com.thurbandeira.todocli.cli;

import com.thurbandeira.todocli.model.Task;
import com.thurbandeira.todocli.service.TaskService;
import com.thurbandeira.todocli.storage.Storage;
import com.thurbandeira.todocli.storage.TaskRepository;

import java.util.List;
import java.util.Scanner;

public class CliApp {

    private final Scanner scanner;
    private final TaskService taskService;
    private final TaskRepository storage;

    public CliApp() {
        this.scanner = new Scanner(System.in);
        this.taskService = new TaskService();
        this.storage = new Storage("data/tasks.json");
    }

    public void run() {
        loadTasks();
        printBanner();
        printLoadedInfo();

        int option;
        do {
            printMenu();
            option = readInt("Escolha uma opcao: ");

            switch (option) {
                case 1 -> {
                    String title = readNonEmpty("Digite o titulo da tarefa: ");
                    taskService.addTask(title);
                    System.out.println("Tarefa adicionada com sucesso!");
                }
                case 2 -> printTasks(taskService.listTasksSorted());
                case 3 -> {
                    int idDone = readPositiveInt("Digite o ID da tarefa para concluir: ");
                    TaskService.MarkResult result = taskService.markTaskAsCompleted(idDone);
                    switch (result) {
                        case MARKED -> System.out.println("Tarefa marcada como concluida!");
                        case ALREADY_COMPLETED -> System.out.println("Essa tarefa ja esta concluida.");
                        case NOT_FOUND -> System.out.println("Nao existe tarefa com esse ID.");
                    }
                }
                case 4 -> {
                    int idRemove = readPositiveInt("Digite o ID da tarefa para remover: ");
                    if (taskService.removeTask(idRemove)) {
                        System.out.println("Tarefa removida com sucesso!");
                    } else {
                        System.out.println("Nao existe tarefa com esse ID.");
                    }
                }
                case 0 -> {
                    storage.save(taskService.getTasks());
                    System.out.println("Tarefas salvas em data/tasks.json");
                    System.out.println("Saindo do sistema...");
                }
                default -> System.out.println("Opcao invalida! Digite um numero de 0 a 4.");
            }
        } while (option != 0);

        scanner.close();
    }

    private void loadTasks() {
        List<Task> loadedTasks = storage.load();
        taskService.setTasks(loadedTasks);

        int maxId = 0;
        for (Task t : loadedTasks) {
            if (t.getId() > maxId) maxId = t.getId();
        }
        taskService.setNextId(maxId + 1);
    }

    private void printLoadedInfo() {
        if (!taskService.getTasks().isEmpty()) {
            System.out.println("Tarefas carregadas: " + taskService.getTasks().size());
        }
    }

    private void printMenu() {
        System.out.println("\n=== GERENCIADOR DE TAREFAS ===");
        System.out.println("1 - Adicionar tarefa");
        System.out.println("2 - Listar tarefas");
        System.out.println("3 - Marcar como concluida");
        System.out.println("4 - Remover tarefa");
        System.out.println("0 - Sair");
    }

    private void printBanner() {
        System.out.println("=================================");
        System.out.println("     GERENCIADOR DE TAREFAS CLI   ");
        System.out.println("=================================");
    }

    private void printTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("Nenhuma tarefa cadastrada.");
            return;
        }

        System.out.println("\n--- LISTA DE TAREFAS ---");
        for (Task task : tasks) {
            String status = task.isCompleted() ? "[X]" : "[ ]";
            String doneText = task.isCompleted() ? " - Concluida" : "";
            System.out.println(status + " " + task.getId() + " - " + task.getTitle() + doneText);
        }

        TaskService.Summary summary = taskService.getSummary();
        System.out.println("\nResumo: " + summary.total() + " total | " + summary.pending()
                + " pendentes | " + summary.done() + " concluidas");
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida! Digite apenas numeros.");
            }
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            int value = readInt(prompt);
            if (value > 0) return value;
            System.out.println("Digite um numero maior que zero.");
        }
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            if (!text.isEmpty()) return text;
            System.out.println("O texto nao pode ficar vazio.");
        }
    }
}
