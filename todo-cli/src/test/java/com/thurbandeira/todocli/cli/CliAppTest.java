package com.thurbandeira.todocli.cli;

import com.thurbandeira.todocli.storage.Storage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CliAppTest {

    @TempDir
    Path tempDir;

    @Test
    void runArgs_addPersistsTask() {
        Path json = tempDir.resolve("tasks.json");
        Storage storage = new Storage(json.toString());
        Scanner scanner = new Scanner(new ByteArrayInputStream(new byte[0]));
        CliApp app = new CliApp(storage, scanner);

        app.run(new String[]{"add", "Tarefa", "X"});

        assertTrue(storage.load().stream().anyMatch(t -> t.getTitle().equals("Tarefa X")));
    }
}
