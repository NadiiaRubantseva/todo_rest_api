package ua.nadiiarubantseva.todo.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskRequest {
    private String description;

    // todo: expand task entity with more fields like title, priority
    //  and add them to the create task request
}
