package ua.nadiiarubantseva.todo.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.nadiiarubantseva.todo.auth.dto.LoginRequest;
import ua.nadiiarubantseva.todo.auth.dto.LoginResponse;
import ua.nadiiarubantseva.todo.auth.dto.RegistrationRequest;
import ua.nadiiarubantseva.todo.auth.dto.RegistrationResponse;

@RequestMapping("api/v1/auth")
@RestController
@Tag(name = "Authentication Controller", description = "Manages user authentication and registration processes")
@RequiredArgsConstructor
public class AuthController {

    private final AuthValidator authValidator;
    private final AuthService authService;

    @Operation(
            summary = "Register a new user",
            description = "This API registers a new user by validating the input and processing the request."
    )
    @ApiResponse(responseCode = "200", description = "Successful registration or validation error",
            content = @Content(schema = @Schema(implementation = RegistrationResponse.class))
    )
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@RequestBody RegistrationRequest request) {
        RegistrationResponse.Error error = authValidator.validate(request);
        if (error != RegistrationResponse.Error.ok) {
            return ResponseEntity.ok().body(RegistrationResponse.fail(error));
        }
        authService.register(request);
        return ResponseEntity.ok(RegistrationResponse.success());
    }

    @Operation(
            summary = "Login a user",
            description = "This API authenticates a user and returns a JWT token on successful login."
    )
    @ApiResponse(responseCode = "200", description = "Successful login or validation error",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse.Error error = authValidator.validate(request);
        if (error != LoginResponse.Error.ok) {
            return ResponseEntity.ok().body(LoginResponse.fail(error));
        }
        String jwtToken = authService.authenticate(request);
        return ResponseEntity.ok(LoginResponse.success(jwtToken));
    }
}
