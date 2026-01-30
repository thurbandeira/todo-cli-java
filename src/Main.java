import service.TaskService;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        TaskService taskService = new TaskService();

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
                    System.out.println("Marcar tarefa (em breve)");
                    break;

                case 4:
                    System.out.println("Remover tarefa (em breve)");
                    break;

                case 0:
                    System.out.println("Saindo do sistema...");
                    break;

                default:
                    System.out.println("Opção inválida!");
            }

        } while (option != 0);

        scanner.close();
    }
}