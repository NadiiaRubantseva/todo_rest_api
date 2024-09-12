package ua.nadiiarubantseva.todo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private boolean success;
    private Error error;
    private String token;

    public enum Error {
        invalidEmail,
        invalidPassword,
        invalidCredentials,
        ok
    }

    public static LoginResponse success(String token) {
        return LoginResponse.builder()
                .success(true)
                .error(Error.ok)
                .token(token)
                .build();
    }

    public static LoginResponse fail(LoginResponse.Error error) {
        return LoginResponse.builder()
                .success(false)
                .error(error)
                .build();
    }
}
