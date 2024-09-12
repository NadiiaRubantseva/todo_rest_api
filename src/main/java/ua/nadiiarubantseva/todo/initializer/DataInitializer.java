package ua.nadiiarubantseva.todo.initializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.nadiiarubantseva.todo.user.User;
import ua.nadiiarubantseva.todo.user.UserRepository;
import ua.nadiiarubantseva.todo.user.role.Role;
import ua.nadiiarubantseva.todo.user.role.RoleRepository;

import java.util.Collections;

@Configuration
public class DataInitializer {

    @Value("${security.user.full-name}")
    private String adminFullName;

    @Value("${security.user.email}")
    private String adminEmail;

    @Value("${security.user.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.findByEmail(adminEmail).isEmpty()) {

                Role adminRole = roleRepository.findByName(Role.Name.ADMIN)
                        .orElseThrow(() -> new IllegalStateException("Admin role not found"));

                User user = User.builder()
                        .fullName(adminFullName)
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .roles(Collections.singleton(adminRole))
                        .build();

                userRepository.save(user);
            }
        };
    }
}