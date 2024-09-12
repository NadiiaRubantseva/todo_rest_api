package ua.nadiiarubantseva.todo.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ua.nadiiarubantseva.todo.auth.dto.LoginRequest;
import ua.nadiiarubantseva.todo.auth.dto.LoginResponse;
import ua.nadiiarubantseva.todo.auth.dto.RegistrationRequest;
import ua.nadiiarubantseva.todo.auth.dto.RegistrationResponse;
import ua.nadiiarubantseva.todo.user.User;
import ua.nadiiarubantseva.todo.user.UserService;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class AuthValidator {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public RegistrationResponse.Error validate(RegistrationRequest request) {
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            return RegistrationResponse.Error.invalidFullName;
        }
        if (isEmailInvalid(request.getEmail())) {
            return RegistrationResponse.Error.invalidEmail;
        }
        if (isPasswordInvalid(request.getPassword())) {
            return RegistrationResponse.Error.invalidPassword;
        }
        if (userService.isUserExist(request.getEmail())) {
            return RegistrationResponse.Error.userAlreadyExists;
        }
        return RegistrationResponse.Error.ok;
    }

    public LoginResponse.Error validate(LoginRequest request) {
        if (isEmailInvalid(request.getEmail())) {
            return LoginResponse.Error.invalidEmail;
        }
        if (isPasswordInvalid(request.getPassword())) {
            return LoginResponse.Error.invalidPassword;
        }
        Optional<User> optionalUser = userService.findUserByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            return LoginResponse.Error.invalidCredentials;
        }
        if (!passwordEncoder.matches(request.getPassword(), optionalUser.get().getPassword())) {
            return LoginResponse.Error.invalidCredentials;
        }
        return LoginResponse.Error.ok;
    }

    private boolean isPasswordInvalid(String password) {
        return password == null || password.isBlank();
    }

    private boolean isEmailInvalid(String email) {
        if (email == null || email.isBlank()) {
            return true;
        }
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return !matcher.matches();
    }
}
