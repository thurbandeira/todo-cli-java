package com.thurbandeira.todocli.cli;

import com.thurbandeira.todocli.model.Task;
import com.thurbandeira.todocli.service.TaskService;
import com.thurbandeira.todocli.storage.Storage;
import com.thurbandeira.todocli.storage.TaskRepository;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class CliApp {

    private final Scanner scanner;
    private final TaskService taskService;
    private final TaskRepository storage;

    public CliApp() {
        this(new Storage("data/tasks.json"), new Scanner(System.in));
    }

    public CliApp(TaskRepository storage, Scanner scanner) {
        this.scanner = scanner;
        this.taskService = new TaskService();
        this.storage = storage;
    }

    public void run(String[] args) {
        loadTasks();
        if (args != null && args.length > 0) {
            handleArgs(args);
            return;
        }

        runInteractive();
    }

    private void runInteractive() {
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
                case 5 -> {
                    int idEdit = readPositiveInt("Digite o ID da tarefa para editar: ");
                    String newTitle = readNonEmpty("Digite o novo titulo da tarefa: ");
                    TaskService.UpdateResult result = taskService.updateTitle(idEdit, newTitle);
                    switch (result) {
                        case UPDATED -> System.out.println("Tarefa atualizada com sucesso!");
                        case NOT_FOUND -> System.out.println("Nao existe tarefa com esse ID.");
                        case INVALID_TITLE -> System.out.println("Titulo invalido.");
                    }
                }
                case 6 -> {
                    String keyword = readNonEmpty("Digite a palavra-chave: ");
                    printTasks(taskService.searchByKeyword(keyword));
                }
                case 7 -> printTasks(taskService.listByStatus(false));
                case 8 -> printTasks(taskService.listByStatus(true));
                case 0 -> {
                    storage.save(taskService.getTasks());
                    System.out.println("Tarefas salvas em data/tasks.json");
                    System.out.println("Saindo do sistema...");
                }
                default -> System.out.println("Opcao invalida! Digite um numero de 0 a 8.");
            }
        } while (option != 0);

        scanner.close();
    }

    private void handleArgs(String[] args) {
        String command = args[0].toLowerCase();
        switch (command) {
            case "help", "--help", "-h" -> printHelp();
            case "add" -> {
                String title = joinArgs(args, 1);
                if (title.isBlank()) {
                    System.out.println("Titulo nao pode ser vazio.");
                    return;
                }
                taskService.addTask(title);
                storage.save(taskService.getTasks());
                System.out.println("Tarefa adicionada com sucesso!");
            }
            case "list" -> printTasks(taskService.listTasksSorted());
            case "pending" -> printTasks(taskService.listByStatus(false));
            case "completed" -> printTasks(taskService.listByStatus(true));
            case "search" -> {
                String keyword = joinArgs(args, 1);
                if (keyword.isBlank()) {
                    System.out.println("Informe a palavra-chave.");
                    return;
                }
                printTasks(taskService.searchByKeyword(keyword));
            }
            case "edit" -> {
                Integer id = parseIdArg(args, 1);
                if (id == null) return;
                String newTitle = joinArgs(args, 2);
                TaskService.UpdateResult result = taskService.updateTitle(id, newTitle);
                switch (result) {
                    case UPDATED -> {
                        storage.save(taskService.getTasks());
                        System.out.println("Tarefa atualizada com sucesso!");
                    }
                    case NOT_FOUND -> System.out.println("Nao existe tarefa com esse ID.");
                    case INVALID_TITLE -> System.out.println("Titulo invalido.");
                }
            }
            case "done" -> {
                Integer id = parseIdArg(args, 1);
                if (id == null) return;
                TaskService.MarkResult result = taskService.markTaskAsCompleted(id);
                switch (result) {
                    case MARKED -> {
                        storage.save(taskService.getTasks());
                        System.out.println("Tarefa marcada como concluida!");
                    }
                    case ALREADY_COMPLETED -> System.out.println("Essa tarefa ja esta concluida.");
                    case NOT_FOUND -> System.out.println("Nao existe tarefa com esse ID.");
                }
            }
            case "remove" -> {
                Integer id = parseIdArg(args, 1);
                if (id == null) return;
                if (taskService.removeTask(id)) {
                    storage.save(taskService.getTasks());
                    System.out.println("Tarefa removida com sucesso!");
                } else {
                    System.out.println("Nao existe tarefa com esse ID.");
                }
            }
            default -> {
                System.out.println("Comando invalido.");
                printHelp();
            }
        }
    }

    private void loadTasks() {
        migrateIfNeeded();
        List<Task> loadedTasks = storage.load();
        taskService.setTasks(loadedTasks);

        int maxId = 0;
        for (Task t : loadedTasks) {
            if (t.getId() > maxId) maxId = t.getId();
        }
        taskService.setNextId(maxId + 1);
    }

    private void migrateIfNeeded() {
        if (!(storage instanceof Storage concreteStorage)) return;

        File jsonFile = new File(concreteStorage.getFilePath());
        String csvPath = jsonFile.getPath().replaceAll("\\.json$", ".csv");
        File csvFile = new File(csvPath);
        if (!jsonFile.exists() && csvFile.exists()) {
            boolean migrated = concreteStorage.migrateFromCsv(csvFile.getPath());
            if (migrated) {
                System.out.println("Migracao concluida: " + csvFile.getPath() + " -> " + jsonFile.getPath());
            }
        }
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
        System.out.println("5 - Editar tarefa");
        System.out.println("6 - Buscar por palavra-chave");
        System.out.println("7 - Listar pendentes");
        System.out.println("8 - Listar concluidas");
        System.out.println("0 - Sair");
    }

    private void printBanner() {
        System.out.println("=================================");
        System.out.println("     GERENCIADOR DE TAREFAS CLI   ");
        System.out.println("=================================");
    }

    private void printHelp() {
        System.out.println("Uso:");
        System.out.println("  todo-cli add \"titulo da tarefa\"");
        System.out.println("  todo-cli list");
        System.out.println("  todo-cli pending");
        System.out.println("  todo-cli completed");
        System.out.println("  todo-cli search \"palavra-chave\"");
        System.out.println("  todo-cli edit <id> \"novo titulo\"");
        System.out.println("  todo-cli done <id>");
        System.out.println("  todo-cli remove <id>");
        System.out.println("  todo-cli help");
    }

    private Integer parseIdArg(String[] args, int index) {
        if (args.length <= index) {
            System.out.println("Informe o ID.");
            return null;
        }
        try {
            int id = Integer.parseInt(args[index]);
            if (id <= 0) {
                System.out.println("ID deve ser maior que zero.");
                return null;
            }
            return id;
        } catch (NumberFormatException e) {
            System.out.println("ID invalido.");
            return null;
        }
    }

    private String joinArgs(String[] args, int start) {
        if (args.length <= start) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString().trim();
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

        int done = 0;
        for (Task task : tasks) {
            if (task.isCompleted()) done++;
        }
        int total = tasks.size();
        int pending = total - done;
        System.out.println("\nResumo: " + total + " total | " + pending + " pendentes | " + done + " concluidas");
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
