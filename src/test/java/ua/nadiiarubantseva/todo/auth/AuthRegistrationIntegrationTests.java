package ua.nadiiarubantseva.todo.auth;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.JoinType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.nadiiarubantseva.todo.auth.dto.RegistrationRequest;
import ua.nadiiarubantseva.todo.auth.dto.RegistrationResponse;
import ua.nadiiarubantseva.todo.user.User;
import ua.nadiiarubantseva.todo.user.UserService;
import ua.nadiiarubantseva.todo.user.role.Role;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthRegistrationIntegrationTests {

    @LocalServerPort
    protected int port;

    private String authRegistrationUrl;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @PostConstruct
    public void postConstruct() {
        this.authRegistrationUrl = "http://localhost:" + port + "/api/v1/auth/register";
    }

    @BeforeEach
    public void beforeEach() {
        userService.deleteAll();
    }

    @Test
    @DisplayName("Test that user cannot register with invalid full name")
    public void testThatUserCannotRegisterWithInvalidFullName() {
        RegistrationRequest requestWithNullFullName = createRegistrationRequest();
        requestWithNullFullName.setFullName(null);
        RegistrationRequest requestWithEmptyFullName = createRegistrationRequest();
        requestWithEmptyFullName.setFullName("   ");

        List<RegistrationRequest> invalidRequests = List.of(requestWithNullFullName, requestWithEmptyFullName);

        for (RegistrationRequest request : invalidRequests) {
            ResponseEntity<RegistrationResponse> response = sendRequest(request);
            assertErrorResponse(RegistrationResponse.Error.invalidFullName, response);
        }
    }

    @Test
    @DisplayName("Test that user cannot register with invalid email")
    public void testThatUserCannotRegisterWithInvalidEmail() {
        RegistrationRequest requestWithNullEmail = createRegistrationRequest();
        requestWithNullEmail.setEmail(null);
        RegistrationRequest requestWithEmptyEmail = createRegistrationRequest();
        requestWithEmptyEmail.setEmail("   ");
        RegistrationRequest requestWithInvalidEmail = createRegistrationRequest();
        requestWithInvalidEmail.setEmail("user.com");

        List<RegistrationRequest> invalidRequests = List.of(
                requestWithNullEmail,
                requestWithEmptyEmail,
                requestWithInvalidEmail
        );

        for (RegistrationRequest request : invalidRequests) {
            ResponseEntity<RegistrationResponse> response = sendRequest(request);
            assertErrorResponse(RegistrationResponse.Error.invalidEmail, response);
        }
    }

    @Test
    @DisplayName("Test that user cannot register with invalid password")
    public void testThatUserCannotRegisterWithInvalidPassword() {
        RegistrationRequest requestWithNullPassword = createRegistrationRequest();
        requestWithNullPassword.setPassword(null);
        RegistrationRequest requestWithEmptyPassword = createRegistrationRequest();
        requestWithEmptyPassword.setPassword("   ");

        List<RegistrationRequest> invalidRequests = List.of(
                requestWithNullPassword,
                requestWithEmptyPassword
        );

        for (RegistrationRequest request : invalidRequests) {
            ResponseEntity<RegistrationResponse> response = sendRequest(request);
            assertErrorResponse(RegistrationResponse.Error.invalidPassword, response);
        }
    }

    @Test
    @DisplayName("Test that user cannot register with email that already exist in db")
    public void testThatUserCannotRegisterWithEmailThatAlreadyExistInDb() {
        RegistrationRequest request = createRegistrationRequest();
        authService.register(request);
        assertTrue(getUserByEmail(request).isPresent());

        ResponseEntity<RegistrationResponse> response = sendRequest(request);

        assertErrorResponse(RegistrationResponse.Error.userAlreadyExists, response);
    }

    @Test
    @DisplayName("Test that user successfully registered with valid request")
    public void testThatUserSuccessfullyRegisteredWithValidRequest() {
        RegistrationRequest request = createRegistrationRequest();
        ResponseEntity<RegistrationResponse> response = sendRequest(request);
        assertSuccessResponse(request, response);
    }

    private void assertSuccessResponse(
            RegistrationRequest request,
            ResponseEntity<RegistrationResponse> actualResponse
    ) {
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertTrue(actualResponse.getBody().isSuccess());
        Optional<User> userOpt = getUserByEmail(request);
        assertTrue(userOpt.isPresent());

        User user = userOpt.get();
        assertEquals(request.getEmail(), user.getEmail());
        assertTrue(passwordEncoder.matches(request.getPassword(), user.getPassword()));
        assertEquals(request.getFullName(), user.getFullName());
        assertEquals(1, user.getRoles().size());
        assertEquals(Role.Name.USER, user.getRoles().stream().findFirst().orElseThrow().getName());
    }

    private Optional<User> getUserByEmail(RegistrationRequest request) {
        List<User> users = userService.findAll((root, query, criteriaBuilder) -> {
            root.fetch(User.Fields.roles, JoinType.LEFT);
            return criteriaBuilder.equal(root.get(User.Fields.email), request.getEmail());
        });
        return Optional.of(users.getFirst());
    }

    private void assertErrorResponse(
            RegistrationResponse.Error expectedError,
            ResponseEntity<RegistrationResponse> actualResponse
    ) {
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertFalse(actualResponse.getBody().isSuccess());
        assertEquals(expectedError, actualResponse.getBody().getError());
    }

    private ResponseEntity<RegistrationResponse> sendRequest(RegistrationRequest request) {
        return restTemplate.exchange(
                authRegistrationUrl,
                HttpMethod.POST,
                new HttpEntity<>(request),
                RegistrationResponse.class
        );
    }

    private RegistrationRequest createRegistrationRequest() {
        return RegistrationRequest.builder()
                .fullName("Full Name")
                .email("email@email.com")
                .password("pass")
                .build();
    }
}
