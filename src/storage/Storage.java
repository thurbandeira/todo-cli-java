package storage;

import model.Task;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Storage {

    private final String filePath;

    public Storage(String filePath) {
        this.filePath = filePath;
    }

    public List<Task> load() {
        List<Task> tasks = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return tasks; // arquivo ainda não existe
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(";", 3);
                if (parts.length < 3) continue;

                int id = Integer.parseInt(parts[0]);
                String title = parts[1];
                boolean completed = Boolean.parseBoolean(parts[2]);

                tasks.add(new Task(id, title, completed));
            }
        } catch (Exception e) {
            System.out.println("Erro ao carregar arquivo: " + e.getMessage());
        }

        return tasks;
    }

    public void save(List<Task> tasks) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (Task task : tasks) {
                bw.write(task.getId() + ";" + task.getTitle() + ";" + task.isCompleted());
                bw.newLine();
            }
        } catch (Exception e) {
            System.out.println("Erro ao salvar arquivo: " + e.getMessage());
        }
    }
}
