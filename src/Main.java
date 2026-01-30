import model.Task;
import service.TaskService;
import storage.Storage;

import java.util.List;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        TaskService taskService = new TaskService();

        Storage storage = new Storage("tasks.csv");

        List<Task> loadedTasks = storage.load();
        taskService.setTasks(loadedTasks);

// ajustar nextId com base no maior ID carregado
        int maxId = 0;
        for (Task t : loadedTasks) {
            if (t.getId() > maxId) maxId = t.getId();
        }
        taskService.setNextId(maxId + 1);

        if (!loadedTasks.isEmpty()) {
            System.out.println("Tarefas carregadas: " + loadedTasks.size());
        }


        int option;

        do {
            System.out.println("\n=== GERENCIADOR DE TAREFAS ===");
            System.out.println("1 - Adicionar tarefa");
            System.out.println("2 - Listar tarefas");
            System.out.println("3 - Marcar como concluída");
            System.out.println("4 - Remover tarefa");
            System.out.println("0 - Sair");

            System.out.print("Escolha uma opção: ");
            option = scanner.nextInt();

            switch (option) {

                case 1:
                    scanner.nextLine(); // limpar buffer
                    System.out.print("Digite o título da tarefa: ");
                    String title = scanner.nextLine();
                    taskService.addTask(title);
                    break;

                case 2:
                    taskService.listTasks();
                    break;

                case 3:
                    System.out.print("Digite o ID da tarefa para concluir: ");
                    int idDone = scanner.nextInt();
                    taskService.markTaskAsCompleted(idDone);
                    break;

                case 4:
                    System.out.print("Digite o ID da tarefa para remover: ");
                    int idRemove = scanner.nextInt();
                    taskService.removeTask(idRemove);
                    break;

                case 0:
                    storage.save(taskService.getTasks());
                    System.out.println("Tarefas salvas em tasks.csv");
                    System.out.println("Saindo do sistema...");
                    break;

                default:
                    System.out.println("Opção inválida!");
            }

        } while (option != 0);

        scanner.close();
    }
}