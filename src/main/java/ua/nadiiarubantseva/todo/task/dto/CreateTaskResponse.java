package ua.nadiiarubantseva.todo.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.nadiiarubantseva.todo.task.dao.Task;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskResponse {

    private boolean success;
    private Error error;
    private Long taskId;

    public enum Error {
        ok,
        invalidDescription
    }

    public static CreateTaskResponse fail(CreateTaskResponse.Error error) {
        return CreateTaskResponse.builder()
                .success(false)
                .error(error)
                .build();
    }

    public static CreateTaskResponse success(Task createdTask) {
        return CreateTaskResponse.builder()
                .success(true)
                .error(Error.ok)
                .taskId(createdTask.getId())
                .build();
    }
}
