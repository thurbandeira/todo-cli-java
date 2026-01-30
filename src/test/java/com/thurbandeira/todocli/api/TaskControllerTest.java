package com.thurbandeira.todocli.api;

import com.thurbandeira.todocli.model.Task;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskManager taskManager;

    @Test
    void listTasks_rejectsInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/tasks").queryParam("status", "unknown"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_returnsCreated() throws Exception {
        Mockito.when(taskManager.addTask("Nova tarefa"))
                .thenReturn(new Task(1, "Nova tarefa"));

        String payload = """
                {"title":"Nova tarefa"}
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Nova tarefa"))
                .andExpect(jsonPath("$.completed").value(false));
    }
}
