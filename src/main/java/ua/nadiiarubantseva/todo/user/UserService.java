package ua.nadiiarubantseva.todo.user;

import lombok.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public boolean isUserExist(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

    public List<User> findAll(Specification<User> spec) {
        return userRepository.findAll(spec);
    }
}
