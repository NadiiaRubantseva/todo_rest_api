package ua.nadiiarubantseva.todo.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteTaskResponse {

    private boolean success;
    private Error error;

    public enum Error {
        ok,
        invalidTaskId
    }

    public static DeleteTaskResponse fail(DeleteTaskResponse.Error error) {
        return DeleteTaskResponse.builder()
                .success(false)
                .error(error)
                .build();
    }

    public static DeleteTaskResponse success() {
        return DeleteTaskResponse.builder()
                .success(true)
                .error(Error.ok)
                .build();
    }
}
