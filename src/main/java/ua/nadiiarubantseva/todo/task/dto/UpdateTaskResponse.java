package ua.nadiiarubantseva.todo.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskResponse {

    private boolean success;
    private Error error;

    public enum Error {
        ok,
        invalidTaskId,
        invalidDescription,
        invalidComplete
    }

    public static UpdateTaskResponse fail(UpdateTaskResponse.Error error) {
        return UpdateTaskResponse.builder()
                .success(false)
                .error(error)
                .build();
    }

    public static UpdateTaskResponse success() {
        return UpdateTaskResponse.builder()
                .success(true)
                .error(Error.ok)
                .build();
    }
}
