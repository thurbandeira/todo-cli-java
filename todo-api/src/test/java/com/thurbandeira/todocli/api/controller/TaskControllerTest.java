package com.thurbandeira.todocli.api.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("storage.file-path", () -> tempDir.resolve("tasks.json").toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void list_rejectsInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/tasks").param("status", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_returnsCreated() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Nova\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Nova"));
    }

    @Test
    void createTask_rejectsBlankTitle() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_requiresKeyword() throws Exception {
        mockMvc.perform(get("/api/tasks/search").param("keyword", ""))
                .andExpect(status().isBadRequest());
    }
}
