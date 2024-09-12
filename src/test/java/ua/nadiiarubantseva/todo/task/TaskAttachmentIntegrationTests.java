package ua.nadiiarubantseva.todo.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ua.nadiiarubantseva.todo.attachment.Attachment;
import ua.nadiiarubantseva.todo.security.jwt.JWTService;
import ua.nadiiarubantseva.todo.task.dao.Task;
import ua.nadiiarubantseva.todo.task.dao.TaskRepository;
import ua.nadiiarubantseva.todo.task.dto.CreateTaskRequest;
import ua.nadiiarubantseva.todo.task.dto.CreateTaskResponse;
import ua.nadiiarubantseva.todo.user.User;
import ua.nadiiarubantseva.todo.user.UserRepository;
import ua.nadiiarubantseva.todo.user.role.Role;
import ua.nadiiarubantseva.todo.user.role.RoleRepository;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskAttachmentIntegrationTests {

    private final static String CREATE_TASK_ENDPOINT = "/api/v1/todo/task";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void testCreateTaskWithAttachment() throws Exception {
        User user = createAndSaveUser();

        CreateTaskRequest request = new CreateTaskRequest();
        request.setDescription("Task Description");
        String requestJson = objectMapper.writeValueAsString(request);

        MockMultipartFile attachment = new MockMultipartFile(
                "attachment",
                "testfile.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Sample file content".getBytes()
        );

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                requestJson.getBytes()
        );

        MvcResult result = mockMvc.perform(multipart(CREATE_TASK_ENDPOINT)
                        .file(attachment)
                        .file(requestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .header("Authorization", "Bearer " + jwtService.generateToken(user)))  // Authorization header
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        CreateTaskResponse response = objectMapper.readValue(responseContent, CreateTaskResponse.class);
        assertTrue(response.isSuccess());
        assertEquals(CreateTaskResponse.Error.ok, response.getError());
        assertNotNull(response.getTaskId());
        Task taskInDB = taskRepository.findById(response.getTaskId()).orElseThrow();
        assertEquals(request.getDescription(), taskInDB.getDescription());

        Attachment savedAttachment = taskRepository.findAll().getFirst().getAttachment();
        assertEquals(savedAttachment.getId(), taskInDB.getAttachment().getId());
    }

    private User createAndSaveUser() {
        Role userRole = roleRepository.findByName(Role.Name.USER)
                .orElseThrow(() -> new IllegalStateException("User role not found"));

        User user = User.builder()
                .email("user@email.com")
                .password(passwordEncoder.encode("pass"))
                .fullName("User")
                .roles(Collections.singleton(userRole))
                .build();

        return userRepository.save(user);
    }
}
