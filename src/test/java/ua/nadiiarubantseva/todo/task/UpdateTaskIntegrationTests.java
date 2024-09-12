package ua.nadiiarubantseva.todo.task;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.nadiiarubantseva.todo.security.jwt.JWTService;
import ua.nadiiarubantseva.todo.task.dao.Task;
import ua.nadiiarubantseva.todo.task.dao.TaskRepository;
import ua.nadiiarubantseva.todo.task.dto.UpdateTaskRequest;
import ua.nadiiarubantseva.todo.task.dto.UpdateTaskResponse;
import ua.nadiiarubantseva.todo.user.User;
import ua.nadiiarubantseva.todo.user.UserRepository;
import ua.nadiiarubantseva.todo.user.role.Role;
import ua.nadiiarubantseva.todo.user.role.RoleRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UpdateTaskIntegrationTests {

    @LocalServerPort
    protected int port;

    private String updateTaskUrl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TaskRepository taskRepository;

    @PostConstruct
    public void postConstruct() {
        this.updateTaskUrl = "http://localhost:" + port + "/api/v1/todo/task/";
    }

    @BeforeEach
    public void beforeEach() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Test that user cannot update task that does not belong to him")
    public void testThatUserCannotUpdateTaskThatDoesNotBelongToHim() {
        User user = createAndSaveUser();
        UpdateTaskRequest request = createUpdateTaskRequest();
        ResponseEntity<UpdateTaskResponse> response = sendRequest(request, user, 1L);
        assertErrorResponse(UpdateTaskResponse.Error.invalidTaskId, response);
    }

    @Test
    @DisplayName("Test that user cannot update task with invalid description")
    public void testThatUserCannotUpdateTaskWithInvalidDescription() {
        User user = createAndSaveUser();
        Task task = createAndSaveTask(user);
        UpdateTaskRequest requestWithNullDescription = createUpdateTaskRequest();
        requestWithNullDescription.setDescription(null);
        UpdateTaskRequest requestWithBlankDescription = createUpdateTaskRequest();
        requestWithBlankDescription.setDescription("   ");
        for (UpdateTaskRequest request : List.of(requestWithNullDescription, requestWithBlankDescription)) {
            ResponseEntity<UpdateTaskResponse> response = sendRequest(request, user, task.getId());
            assertErrorResponse(UpdateTaskResponse.Error.invalidDescription, response);
        }
    }

    @Test
    @DisplayName("Test that user cannot update task with invalid complete")
    public void testThatUserCannotUpdateTaskWithInvalidComplete() {
        User user = createAndSaveUser();
        Task task = createAndSaveTask(user);
        UpdateTaskRequest request = createUpdateTaskRequest();
        request.setComplete(null);
        ResponseEntity<UpdateTaskResponse> response = sendRequest(request, user, task.getId());
        assertErrorResponse(UpdateTaskResponse.Error.invalidComplete, response);
    }

    @Test
    @DisplayName("Test that user can update task with valid data")
    public void testThatUserCanUpdateTaskWithValidData() {
        User user = createAndSaveUser();
        Task task = createAndSaveTask(user);
        UpdateTaskRequest request = createUpdateTaskRequest();
        ResponseEntity<UpdateTaskResponse> response = sendRequest(request, user, task.getId());
        assertSuccessResponse(task.getId(), request, response);
    }

    private void assertSuccessResponse(
            Long taskId,
            UpdateTaskRequest request,
            ResponseEntity<UpdateTaskResponse> response
    ) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(UpdateTaskResponse.Error.ok, response.getBody().getError());
        Task updatedTask = taskRepository.findById(taskId).orElseThrow();
        assertEquals(request.getDescription(), updatedTask.getDescription());
        assertEquals(request.getComplete(), updatedTask.isComplete());
    }


    private Task createAndSaveTask(User user) {
        Task task = Task.builder()
                .user(user)
                .description("todo")
                .build();
        return taskRepository.save(task);
    }

    private void assertErrorResponse(
            UpdateTaskResponse.Error expectedError,
            ResponseEntity<UpdateTaskResponse> actualResponse
    ) {
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertFalse(actualResponse.getBody().isSuccess());
        assertEquals(expectedError, actualResponse.getBody().getError());
    }

    private ResponseEntity<UpdateTaskResponse> sendRequest(
            UpdateTaskRequest request,
            User user,
            Long taskId
    ) {
        HttpHeaders headers = createHeaders(user);
        HttpEntity<?> entity = new HttpEntity<>(request, headers);
        return restTemplate.exchange(
                updateTaskUrl + taskId,
                HttpMethod.PUT,
                entity,
                UpdateTaskResponse.class
        );
    }

    private HttpHeaders createHeaders(User user) {
        HttpHeaders headers = new HttpHeaders();
        if (user != null) {
            String token = jwtService.generateToken(user);
            String authTokenValue = "Bearer " + token;
            headers.set(HttpHeaders.AUTHORIZATION, authTokenValue);
        }
        return headers;
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

    private UpdateTaskRequest createUpdateTaskRequest() {
        return UpdateTaskRequest.builder()
                .description("Water plants and garden")
                .complete(true)
                .build();
    }
}
