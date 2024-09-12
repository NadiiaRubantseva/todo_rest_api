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
import ua.nadiiarubantseva.todo.task.dto.DeleteTaskResponse;
import ua.nadiiarubantseva.todo.user.User;
import ua.nadiiarubantseva.todo.user.UserRepository;
import ua.nadiiarubantseva.todo.user.role.Role;
import ua.nadiiarubantseva.todo.user.role.RoleRepository;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeleteTaskIntegrationTests {

    @LocalServerPort
    protected int port;

    private String deleteTaskUrl;

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
        this.deleteTaskUrl = "http://localhost:" + port + "/api/v1/todo/task/";
    }

    @BeforeEach
    public void beforeEach() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Test that user cannot delete task that does not belong to him")
    public void testThatUserCannotDeleteTaskThatDoesNotBelongToHim() {
        User user = createAndSaveUser();
        ResponseEntity<DeleteTaskResponse> response = sendRequest(user, 1L);
        assertErrorResponse(DeleteTaskResponse.Error.invalidTaskId, response);
    }

    @Test
    @DisplayName("Test that user can delete its task by id")
    public void testThatUserCanDeleteItsTaskById() {
        User user = createAndSaveUser();
        Task task = createAndSaveTask(user);
        ResponseEntity<DeleteTaskResponse> response = sendRequest(user, task.getId());
        assertSuccessResponse(task, response);
    }

    private void assertSuccessResponse(Task task, ResponseEntity<DeleteTaskResponse> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(DeleteTaskResponse.Error.ok, response.getBody().getError());
        assertFalse(taskRepository.existsById(task.getId()));
    }

    private void assertErrorResponse(
            DeleteTaskResponse.Error expectedError,
            ResponseEntity<DeleteTaskResponse> actualResponse
    ) {
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertFalse(actualResponse.getBody().isSuccess());
        assertEquals(expectedError, actualResponse.getBody().getError());
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

    private Task createAndSaveTask(User user) {
        Task task = Task.builder()
                .user(user)
                .description("todo")
                .build();
        return taskRepository.save(task);
    }

    private ResponseEntity<DeleteTaskResponse> sendRequest(
            User user,
            Long taskId
    ) {
        HttpHeaders headers = createHeaders(user);
        HttpEntity<?> entity = new HttpEntity<>("", headers);
        return restTemplate.exchange(
                deleteTaskUrl + taskId,
                HttpMethod.DELETE,
                entity,
                DeleteTaskResponse.class
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

}
