package ua.nadiiarubantseva.todo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponse {

    private boolean success;
    private Error error;

    public enum Error {
        ok,
        invalidFullName,
        invalidEmail,
        invalidPassword,
        userAlreadyExists,
    }

    public static RegistrationResponse success() {
        return RegistrationResponse.builder()
                .success(true)
                .error(Error.ok)
                .build();
    }

    public static RegistrationResponse fail(RegistrationResponse.Error error) {
        return RegistrationResponse.builder()
                .success(false)
                .error(error)
                .build();
    }
}
