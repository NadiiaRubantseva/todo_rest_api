package ua.nadiiarubantseva.todo.auth;

import jakarta.annotation.PostConstruct;
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
import ua.nadiiarubantseva.todo.auth.dto.LoginRequest;
import ua.nadiiarubantseva.todo.auth.dto.LoginResponse;
import ua.nadiiarubantseva.todo.security.jwt.JWTService;
import ua.nadiiarubantseva.todo.user.User;
import ua.nadiiarubantseva.todo.user.UserRepository;
import ua.nadiiarubantseva.todo.user.role.Role;
import ua.nadiiarubantseva.todo.user.role.RoleRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthLoginIntegrationTests {

    @LocalServerPort
    protected int port;

    private String authLoginUrl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;

    @PostConstruct
    public void postConstruct() {
        this.authLoginUrl = "http://localhost:" + port + "/api/v1/auth/login";
    }

    @BeforeEach
    public void beforeEach() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Test that user cannot login with invalid email")
    public void testThatUserCannotLoginWithInvalidEmail() {
        LoginRequest requestWithNullEmail = createLoginRequest();
        requestWithNullEmail.setEmail(null);
        LoginRequest requestWithEmptyEmail = createLoginRequest();
        requestWithEmptyEmail.setEmail("   ");

        List<LoginRequest> invalidRequests = List.of(requestWithNullEmail, requestWithEmptyEmail);

        for (LoginRequest request : invalidRequests) {
            ResponseEntity<LoginResponse> response = sendRequest(request);
            assertErrorResponse(LoginResponse.Error.invalidEmail, response);
        }
    }

    @Test
    @DisplayName("Test that user cannot login with invalid password")
    public void testThatUserCannotLoginWithInvalidPassword() {
        LoginRequest requestWithNullPassword = createLoginRequest();
        requestWithNullPassword.setPassword(null);
        LoginRequest requestWithEmptyPassword = createLoginRequest();
        requestWithEmptyPassword.setPassword("   ");

        List<LoginRequest> invalidRequests = List.of(requestWithNullPassword, requestWithEmptyPassword);

        for (LoginRequest request : invalidRequests) {
            ResponseEntity<LoginResponse> response = sendRequest(request);
            assertErrorResponse(LoginResponse.Error.invalidPassword, response);
        }
    }

    @Test
    @DisplayName("Test that user cannot login with email that does not exist in db")
    public void testThatUserCannotLoginWithEmailThatDoesNotExistInDB() {
        LoginRequest request = createLoginRequest();
        ResponseEntity<LoginResponse> response = sendRequest(request);
        assertErrorResponse(LoginResponse.Error.invalidCredentials, response);
    }

    @Test
    @DisplayName("Test that user cannot login with password that does not match password in db")
    public void testThatUserCannotLoginWithPasswordThatDoesNotMatchPassword() {
        LoginRequest request = createLoginRequest();

        User user = User.builder()
                .fullName("User")
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword() + "."))
                .build();
        userRepository.save(user);

        ResponseEntity<LoginResponse> response = sendRequest(request);
        assertErrorResponse(LoginResponse.Error.invalidCredentials, response);
    }

    @Test
    @DisplayName("Test that user successfully login with valid data")
    public void testThatUserSuccessfullyLoginWithValidData() {
        LoginRequest request = createLoginRequest();

        Role userRole = roleRepository.findByName(Role.Name.USER)
                .orElseThrow(() -> new IllegalStateException("User role not found"));

        User user = User.builder()
                .fullName("User")
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton(userRole))
                .build();
        user = userRepository.save(user);

        ResponseEntity<LoginResponse> response = sendRequest(request);

        String expectedToken = jwtService.generateToken(user);
        assertSuccessResponse(expectedToken, response);
    }

    private void assertSuccessResponse(
            String expectedToken,
            ResponseEntity<LoginResponse> actualResponse
    ) {
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertTrue(actualResponse.getBody().isSuccess());
        assertEquals(LoginResponse.Error.ok, actualResponse.getBody().getError());
        assertEquals(expectedToken, actualResponse.getBody().getToken());
    }

    private void assertErrorResponse(
            LoginResponse.Error expectedError,
            ResponseEntity<LoginResponse> actualResponse
    ) {
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertNotNull(actualResponse.getBody());
        assertFalse(actualResponse.getBody().isSuccess());
        assertEquals(expectedError, actualResponse.getBody().getError());
    }

    private ResponseEntity<LoginResponse> sendRequest(LoginRequest request) {
        return restTemplate.exchange(
                authLoginUrl,
                HttpMethod.POST,
                new HttpEntity<>(request),
                LoginResponse.class
        );
    }

    private LoginRequest createLoginRequest() {
        return LoginRequest.builder()
                .email("email@email.com")
                .password("pass")
                .build();
    }
}
