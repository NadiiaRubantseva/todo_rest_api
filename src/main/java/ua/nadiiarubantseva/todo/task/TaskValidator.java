package ua.nadiiarubantseva.todo.task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.nadiiarubantseva.todo.task.dao.TaskRepository;
import ua.nadiiarubantseva.todo.task.dto.CreateTaskRequest;
import ua.nadiiarubantseva.todo.task.dto.CreateTaskResponse;
import ua.nadiiarubantseva.todo.task.dto.UpdateTaskRequest;
import ua.nadiiarubantseva.todo.task.dto.UpdateTaskResponse;

@Component
@RequiredArgsConstructor
public class TaskValidator {

    private final TaskRepository taskRepository;

    public CreateTaskResponse.Error validate(CreateTaskRequest request) {
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            return CreateTaskResponse.Error.invalidDescription;
        }
        return CreateTaskResponse.Error.ok;
    }

    public UpdateTaskResponse.Error validate(
            UpdateTaskRequest request,
            Long taskId,
            String userEmail
    ) {
        if (isTaskDoesNotBelongToUser(userEmail, taskId)) {
            return UpdateTaskResponse.Error.invalidTaskId;
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            return UpdateTaskResponse.Error.invalidDescription;
        }
        if (request.getComplete() == null) {
            return UpdateTaskResponse.Error.invalidComplete;
        }
        return UpdateTaskResponse.Error.ok;
    }

    public boolean isTaskDoesNotBelongToUser(String userEmail, Long taskId) {
        return !taskRepository.existsByUserEmailAndId(userEmail, taskId);
    }
}
