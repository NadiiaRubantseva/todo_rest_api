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
import ua.nadiiarubantseva.todo.task.dto.GetTaskResponse;
import ua.nadiiarubantseva.todo.user.User;
import ua.nadiiarubantseva.todo.user.UserRepository;
import ua.nadiiarubantseva.todo.user.role.Role;
import ua.nadiiarubantseva.todo.user.role.RoleRepository;

import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetTaskIntegrationTests {

    @LocalServerPort
    protected int port;

    private String getTaskUrl;

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
        this.getTaskUrl = "http://localhost:" + port + "/api/v1/todo/task/";
    }

    @BeforeEach
    public void beforeEach() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Test that user cannot get task that does not belong to him")
    public void testThatUserCannotGetTaskThatDoesNotBelongToHim() {
        User user = createAndSaveUser();
        ResponseEntity<GetTaskResponse> response = sendRequest(user, 1L);
        assertErrorResponse(GetTaskResponse.Error.invalidTaskId, response);
    }

    @Test
    @DisplayName("Test that user can get its task by id")
    public void testThatUserCanGetItsTaskById() {
        User user = createAndSaveUser();
        Task task = createAndSaveTask(user);
        ResponseEntity<GetTaskResponse> response = sendRequest(user, task.getId());
        assertSuccessResponse(task, response);
    }

    private void assertSuccessResponse(Task task, ResponseEntity<GetTaskResponse> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(GetTaskResponse.Error.ok, response.getBody().getError());
        assertTask(task, response.getBody().getTask());
    }

    private void assertTask(Task expectedTask, GetTaskResponse.TaskDto actualTaskDto) {
        assertEquals(expectedTask.getId(), actualTaskDto.getId());
        assertEquals(expectedTask.getDescription(), actualTaskDto.getDescription());
        assertEquals(expectedTask.isComplete(), actualTaskDto.isComplete());
        assertEquals(
                expectedTask.getCreatedAt().truncatedTo(ChronoUnit.SECONDS),
                actualTaskDto.getCreatedAt().truncatedTo(ChronoUnit.SECONDS)
        );
    }

    private void assertErrorResponse(
            GetTaskResponse.Error expectedError,
            ResponseEntity<GetTaskResponse> actualResponse
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

    private ResponseEntity<GetTaskResponse> sendRequest(
            User user,
            Long taskId
    ) {
        HttpHeaders headers = createHeaders(user);
        HttpEntity<?> entity = new HttpEntity<>("", headers);
        return restTemplate.exchange(
                getTaskUrl + taskId,
                HttpMethod.GET,
                entity,
                GetTaskResponse.class
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
