package ua.nadiiarubantseva.todo.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.nadiiarubantseva.todo.user.User;

import java.util.Date;
import java.util.List;

@Service
public class JWTService {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    public String generateToken(User user) {
        List<String> roles = user.getRoles().stream().map(role -> role.getName().name()).toList();
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        Date expiration = new Date(System.currentTimeMillis() + jwtExpiration);
        return JWT.create()
                .withSubject(user.getEmail())
                .withArrayClaim("roles", roles.toArray(new String[0]))
                .withClaim("fullName", user.getFullName())
                .withExpiresAt(expiration)
                .sign(algorithm);
    }
}
