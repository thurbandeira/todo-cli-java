import model.Task;
import service.TaskService;
import storage.Storage;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        TaskService taskService = new TaskService();

        Storage storage = new Storage("data/tasks.csv");

        // carregar
        List<Task> loadedTasks = storage.load();
        taskService.setTasks(loadedTasks);

        int maxId = 0;
        for (Task t : loadedTasks) {
            if (t.getId() > maxId) maxId = t.getId();
        }
        taskService.setNextId(maxId + 1);

        printBanner();

        if (!loadedTasks.isEmpty()) {
            System.out.println("Tarefas carregadas: " + loadedTasks.size());
        }

        int option;

        do {
            printMenu();
            option = readInt(scanner, "Escolha uma opção: ");

            switch (option) {
                case 1 -> {
                    String title = readNonEmpty(scanner, "Digite o título da tarefa: ");
                    taskService.addTask(title);
                }
                case 2 -> taskService.listTasks();

                case 3 -> {
                    int idDone = readPositiveInt(scanner, "Digite o ID da tarefa para concluir: ");
                    taskService.markTaskAsCompleted(idDone);
                }

                case 4 -> {
                    int idRemove = readPositiveInt(scanner, "Digite o ID da tarefa para remover: ");
                    taskService.removeTask(idRemove);
                }

                case 0 -> {
                    storage.save(taskService.getTasks());
                    System.out.println("Tarefas salvas em data/tasks.csv");
                    System.out.println("Saindo do sistema...");
                }

                default -> System.out.println("Opção inválida! Digite um número de 0 a 4.");
            }

        } while (option != 0);

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n=== GERENCIADOR DE TAREFAS ===");
        System.out.println("1 - Adicionar tarefa");
        System.out.println("2 - Listar tarefas");
        System.out.println("3 - Marcar como concluída");
        System.out.println("4 - Remover tarefa");
        System.out.println("0 - Sair");
    }

    private static void printBanner() {
        System.out.println("=================================");
        System.out.println("     GERENCIADOR DE TAREFAS CLI   ");
        System.out.println("=================================");
    }


    // Lê um inteiro sem quebrar o programa (aceita só números)
    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida! Digite apenas números.");
            }
        }
    }

    // Lê inteiro > 0 (para IDs)
    private static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            int value = readInt(scanner, prompt);
            if (value > 0) return value;
            System.out.println("Digite um número maior que zero.");
        }
    }

    // Lê texto não vazio
    private static String readNonEmpty(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            if (!text.isEmpty()) return text;
            System.out.println("O texto não pode ficar vazio.");
        }
    }
}