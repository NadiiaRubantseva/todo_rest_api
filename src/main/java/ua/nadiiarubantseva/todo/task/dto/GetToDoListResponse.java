package ua.nadiiarubantseva.todo.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.nadiiarubantseva.todo.task.dao.Task;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetToDoListResponse {

    private List<TaskDto> tasks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskDto {
        private Long id;
        private String description;
        private Boolean complete;

        public static TaskDto from(Task task) {
            return TaskDto.builder()
                    .id(task.getId())
                    .description(task.getDescription())
                    .complete(task.isComplete())
                    .build();
        }
    }

    public static GetToDoListResponse success(List<Task> tasks) {
        return GetToDoListResponse.builder()
                .tasks(tasks.stream().map(TaskDto::from).toList())
                .build();
    }
}
