package ua.nadiiarubantseva.todo.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.nadiiarubantseva.todo.task.dao.Task;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetTaskResponse {

    private boolean success;
    private Error error;
    private TaskDto task;

    public enum Error {
        ok,
        invalidTaskId
    }

    public static GetTaskResponse fail(Error error) {
        return GetTaskResponse.builder()
                .success(false)
                .error(error)
                .build();
    }

    public static GetTaskResponse success(Task task) {
        return GetTaskResponse.builder()
                .success(true)
                .error(Error.ok)
                .task(TaskDto.from(task))
                .build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskDto {
        private long id;
        private String description;
        private boolean complete;
        private LocalDateTime createdAt;

        public static GetTaskResponse.TaskDto from(Task task) {
            return GetTaskResponse.TaskDto.builder()
                    .id(task.getId())
                    .description(task.getDescription())
                    .complete(task.isComplete())
                    .createdAt(task.getCreatedAt())
                    .build();
        }
    }
}
