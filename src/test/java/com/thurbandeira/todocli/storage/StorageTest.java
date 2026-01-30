package com.thurbandeira.todocli.storage;

import com.thurbandeira.todocli.model.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StorageTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoad_roundTripsTasksWithSpecialChars() {
        Path file = tempDir.resolve("tasks.json");
        Storage storage = new Storage(file.toString());

        Task a = new Task(1, "Titulo com \"aspas\" e \\ barra");
        Task b = new Task(2, "Linha 1\nLinha 2", true);

        storage.save(List.of(a, b));
        List<Task> loaded = storage.load();

        assertEquals(2, loaded.size());
        assertEquals(a.getId(), loaded.get(0).getId());
        assertEquals(a.getTitle(), loaded.get(0).getTitle());
        assertEquals(a.isCompleted(), loaded.get(0).isCompleted());
        assertEquals(b.getId(), loaded.get(1).getId());
        assertEquals(b.getTitle(), loaded.get(1).getTitle());
        assertEquals(b.isCompleted(), loaded.get(1).isCompleted());
    }

    @Test
    void load_returnsEmptyWhenFileMissing() {
        Path file = tempDir.resolve("missing.json");
        Storage storage = new Storage(file.toString());

        List<Task> loaded = storage.load();

        assertTrue(loaded.isEmpty());
    }
}
