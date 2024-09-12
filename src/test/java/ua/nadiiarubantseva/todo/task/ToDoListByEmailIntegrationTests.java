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
import ua.nadiiarubantseva.todo.task.dto.GetToDoListResponse;
import ua.nadiiarubantseva.todo.user.User;
import ua.nadiiarubantseva.todo.user.UserRepository;
import ua.nadiiarubantseva.todo.user.role.Role;
import ua.nadiiarubantseva.todo.user.role.RoleRepository;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ToDoListByEmailIntegrationTests {

    @LocalServerPort
    protected int port;

    private String toDoListByEmailUrl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JWTService jwtService;

    @PostConstruct
    public void postConstruct() {
        this.toDoListByEmailUrl = "http://localhost:" + port + "/api/v1/todo";
    }

    @BeforeEach
    public void beforeEach() {
        userRepository.deleteAll();
    }

    @DisplayName("Test that unauthorized user cannot list todos list")
    @Test
    public void unauthorizedUserCannotListTodos() {
        User user = User.builder()
                .email("unauthorizedUser@example.com")
                .fullName("")
                .password(passwordEncoder.encode("pass"))
                .roles(Collections.emptySet())
                .build();
        ResponseEntity<GetToDoListResponse> response = sendRequest(user);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @DisplayName("Test that user can list his todos list")
    @Test
    public void testThatUserCanListHisTodos() {
        User user = createAndSaveUser();
        List<Task> tasks = createAndSaveTasks(3, user);
        ResponseEntity<GetToDoListResponse> response = sendRequest(user);
        assertResponse(tasks, response);
    }

    private void assertResponse(
            List<Task> tasksInDB,
            ResponseEntity<GetToDoListResponse> actualResponse
    ) {
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());

        List<GetToDoListResponse.TaskDto> expectedTasks = tasksInDB.stream()
                .sorted(comparing(Task::getCreatedAt))
                .map(GetToDoListResponse.TaskDto::from)
                .toList();
        List<GetToDoListResponse.TaskDto> actualTasks = actualResponse.getBody().getTasks();

        assertEquals(expectedTasks, actualTasks);
    }

    private List<Task> createAndSaveTasks(
            int countOfTasksToCreate,
            User user
    ) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < countOfTasksToCreate; i++) {
            tasks.add(Task.builder()
                    .description("task #" + i)
                    .user(user)
                    .createdAt(LocalDateTime.now().plusMinutes(i))
                    .build());
        }
        return taskRepository.saveAll(tasks);
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

    private ResponseEntity<GetToDoListResponse> sendRequest(User user) {
        HttpHeaders headers = createHeaders(user);
        HttpEntity<?> entity = new HttpEntity<>("", headers);
        return restTemplate.exchange(
                toDoListByEmailUrl,
                HttpMethod.GET,
                entity,
                GetToDoListResponse.class
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
