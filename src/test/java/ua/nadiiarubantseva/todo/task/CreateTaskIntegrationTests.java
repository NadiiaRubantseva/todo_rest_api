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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ua.nadiiarubantseva.todo.attachment.AttachmentRepository;
import ua.nadiiarubantseva.todo.security.jwt.JWTService;
import ua.nadiiarubantseva.todo.task.dao.TaskRepository;
import ua.nadiiarubantseva.todo.task.dto.CreateTaskRequest;
import ua.nadiiarubantseva.todo.task.dto.CreateTaskResponse;
import ua.nadiiarubantseva.todo.user.User;
import ua.nadiiarubantseva.todo.user.UserRepository;
import ua.nadiiarubantseva.todo.user.role.Role;
import ua.nadiiarubantseva.todo.user.role.RoleRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateTaskIntegrationTests {

    @LocalServerPort
    protected int port;

    private String createTaskUrl;

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

    @Autowired
    private AttachmentRepository attachmentRepository;

    @PostConstruct
    public void postConstruct() {
        this.createTaskUrl = "http://localhost:" + port + "/api/v1/todo/task";
    }

    @BeforeEach
    public void beforeEach() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Test that user cannot create new task with invalid description")
    public void testThatUserCannotCreateNewTaskWithInvalidDescription() {
        User user = createAndSaveUser();
        CreateTaskRequest requestWithNullDescription = createTaskRequest();
        requestWithNullDescription.setDescription(null);
        CreateTaskRequest requestWithBlankDescription = createTaskRequest();
        requestWithBlankDescription.setDescription("   ");
        for (CreateTaskRequest request : List.of(requestWithNullDescription, requestWithBlankDescription)) {
            ResponseEntity<CreateTaskResponse> response = sendRequest(request, user);
            assertErrorResponse(CreateTaskResponse.Error.invalidDescription, response);
        }
    }

    @Test
    @DisplayName("Test that user can create new task with valid description")
    public void testThatUserCanCreateNewTaskWithValidDescription() {
        User user = createAndSaveUser();
        CreateTaskRequest request = createTaskRequest();
        ResponseEntity<CreateTaskResponse> response = sendRequest(request, user);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        CreateTaskResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals(CreateTaskResponse.Error.ok, body.getError());
        assertTrue(taskRepository.existsById(body.getTaskId()));
        assertTrue(attachmentRepository.findAll().isEmpty());
    }


    private void assertErrorResponse(
            CreateTaskResponse.Error expectedError,
            ResponseEntity<CreateTaskResponse> actualResponse
    ) {
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertFalse(actualResponse.getBody().isSuccess());
        assertEquals(expectedError, actualResponse.getBody().getError());
        assertNull(actualResponse.getBody().getTaskId());
    }

    private ResponseEntity<CreateTaskResponse> sendRequest(CreateTaskRequest request, User user) {
        HttpHeaders headers = createHeaders(user);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("request", new HttpEntity<>(request, createJsonHeaders()));
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(
                createTaskUrl,
                HttpMethod.POST,
                entity,
                CreateTaskResponse.class
        );
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
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

    private CreateTaskRequest createTaskRequest() {
        return CreateTaskRequest.builder()
                .description("Water plants")
                .build();
    }
}
