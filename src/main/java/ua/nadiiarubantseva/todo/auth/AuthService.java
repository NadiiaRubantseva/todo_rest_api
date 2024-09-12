package ua.nadiiarubantseva.todo.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nadiiarubantseva.todo.auth.dto.LoginRequest;
import ua.nadiiarubantseva.todo.auth.dto.RegistrationRequest;
import ua.nadiiarubantseva.todo.security.jwt.JWTService;
import ua.nadiiarubantseva.todo.user.User;
import ua.nadiiarubantseva.todo.user.UserRepository;
import ua.nadiiarubantseva.todo.user.role.Role;
import ua.nadiiarubantseva.todo.user.role.RoleRepository;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @Transactional
    public void register(RegistrationRequest request) {
        try {
            Role userRole = roleRepository.findByName(Role.Name.USER)
                    .orElseThrow(() -> new RuntimeException("User role not found"));

            User user = User.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .roles(Collections.singleton(userRole))
                    .build();

            userRepository.save(user);
        } catch (Throwable e) {
            log.error("Error occurred while registering user", e);
            throw e;
        }
    }

    public String authenticate(LoginRequest request) {
        Authentication authenticationRequest = new UsernamePasswordAuthenticationToken(
                request.getEmail(), request.getPassword()
        );
        authenticationManager.authenticate(authenticationRequest);
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return jwtService.generateToken(user);
    }
}
