package com.thurbandeira.todocli.storage;
import com.thurbandeira.todocli.model.Task;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Storage implements TaskRepository {

    private final String filePath;

    public Storage(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public List<Task> load() {
        List<Task> tasks = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return tasks; // arquivo ainda não existe
        }

        try (BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            String json = sb.toString().trim();
            if (!json.isEmpty()) {
                tasks.addAll(parseTasksJson(json));
            }
        } catch (Exception e) {
            System.out.println("Erro ao carregar arquivo: " + e.getMessage());
        }

        return tasks;
    }

    public void save(List<Task> tasks) {
        try {
            File file = new File(filePath);

            // cria a pasta (data/) se não existir
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            backupExisting(file);

            try (BufferedWriter bw = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                bw.write(toJson(tasks));
            }

        } catch (Exception e) {
            System.out.println("Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    public boolean migrateFromCsv(String csvPath) {
        File csvFile = new File(csvPath);
        if (!csvFile.exists()) return false;

        List<Task> tasks = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(csvFile.toPath(), StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(";", 3);
                if (parts.length < 3) continue;
                try {
                    int id = Integer.parseInt(parts[0]);
                    String title = parts[1];
                    boolean completed = Boolean.parseBoolean(parts[2]);
                    tasks.add(new Task(id, title, completed));
                } catch (NumberFormatException ignored) {
                    // ignora linha malformada
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao migrar CSV: " + e.getMessage());
            return false;
        }

        save(tasks);
        return true;
    }

    private static String toJson(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            sb.append("  {\"id\":")
                    .append(t.getId())
                    .append(",\"title\":\"")
                    .append(escapeJson(t.getTitle()))
                    .append("\",\"completed\":")
                    .append(t.isCompleted())
                    .append("}");
            if (i < tasks.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapeJson(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private static List<Task> parseTasksJson(String json) {
        List<Task> tasks = new ArrayList<>();
        int i = 0;
        i = skipWs(json, i);
        if (i >= json.length() || json.charAt(i) != '[') return tasks;
        i++;
        while (true) {
            i = skipWs(json, i);
            if (i >= json.length()) return tasks;
            if (json.charAt(i) == ']') return tasks;
            if (json.charAt(i) != '{') return tasks;
            i++;

            Integer id = null;
            String title = null;
            Boolean completed = null;

            while (true) {
                i = skipWs(json, i);
                if (i >= json.length()) return tasks;
                if (json.charAt(i) == '}') {
                    i++;
                    break;
                }
                String key;
                if (json.charAt(i) != '"') return tasks;
                ParseResult<String> keyRes = readString(json, i);
                key = keyRes.value;
                i = keyRes.next;
                i = skipWs(json, i);
                if (i >= json.length() || json.charAt(i) != ':') return tasks;
                i++;
                i = skipWs(json, i);

                switch (key) {
                    case "id" -> {
                        ParseResult<Integer> numRes = readNumber(json, i);
                        id = numRes.value;
                        i = numRes.next;
                    }
                    case "title" -> {
                        ParseResult<String> strRes = readString(json, i);
                        title = strRes.value;
                        i = strRes.next;
                    }
                    case "completed" -> {
                        ParseResult<Boolean> boolRes = readBoolean(json, i);
                        completed = boolRes.value;
                        i = boolRes.next;
                    }
                    default -> {
                        // skip unknown value (string/number/boolean/null)
                        ParseResult<Object> skipRes = readUnknownValue(json, i);
                        i = skipRes.next;
                    }
                }

                i = skipWs(json, i);
                if (i >= json.length()) return tasks;
                if (json.charAt(i) == ',') {
                    i++;
                    continue;
                }
                if (json.charAt(i) == '}') {
                    i++;
                    break;
                }
                return tasks;
            }

            if (id != null && title != null && completed != null) {
                tasks.add(new Task(id, title, completed));
            }

            i = skipWs(json, i);
            if (i >= json.length()) return tasks;
            if (json.charAt(i) == ',') {
                i++;
                continue;
            }
            if (json.charAt(i) == ']') return tasks;
        }
    }

    private static int skipWs(String s, int i) {
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c != ' ' && c != '\n' && c != '\r' && c != '\t') return i;
            i++;
        }
        return i;
    }

    private static ParseResult<String> readString(String s, int i) {
        if (s.charAt(i) != '"') return new ParseResult<>("", i);
        i++;
        StringBuilder sb = new StringBuilder();
        while (i < s.length()) {
            char c = s.charAt(i++);
            if (c == '"') break;
            if (c == '\\' && i < s.length()) {
                char e = s.charAt(i++);
                switch (e) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(e); break;
                }
            } else {
                sb.append(c);
            }
        }
        return new ParseResult<>(sb.toString(), i);
    }

    private static ParseResult<Integer> readNumber(String s, int i) {
        int start = i;
        if (i < s.length() && s.charAt(i) == '-') i++;
        while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
        String num = s.substring(start, i);
        try {
            return new ParseResult<>(Integer.parseInt(num), i);
        } catch (NumberFormatException e) {
            return new ParseResult<>(0, i);
        }
    }

    private static ParseResult<Boolean> readBoolean(String s, int i) {
        if (s.startsWith("true", i)) return new ParseResult<>(true, i + 4);
        if (s.startsWith("false", i)) return new ParseResult<>(false, i + 5);
        return new ParseResult<>(false, i);
    }

    private static ParseResult<Object> readUnknownValue(String s, int i) {
        if (i >= s.length()) return new ParseResult<>(null, i);
        char c = s.charAt(i);
        if (c == '"') {
            ParseResult<String> r = readString(s, i);
            return new ParseResult<>(r.value, r.next);
        }
        if (c == '-' || Character.isDigit(c)) {
            ParseResult<Integer> r = readNumber(s, i);
            return new ParseResult<>(r.value, r.next);
        }
        if (s.startsWith("true", i)) return new ParseResult<>(true, i + 4);
        if (s.startsWith("false", i)) return new ParseResult<>(false, i + 5);
        if (s.startsWith("null", i)) return new ParseResult<>(null, i + 4);
        return new ParseResult<>(null, i + 1);
    }

    private static class ParseResult<T> {
        final T value;
        final int next;

        ParseResult(T value, int next) {
            this.value = value;
            this.next = next;
        }
    }

    private void backupExisting(File file) throws IOException {
        if (!file.exists()) return;
        Path source = file.toPath();
        Path target = Path.of(file.getPath() + ".bak");
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
