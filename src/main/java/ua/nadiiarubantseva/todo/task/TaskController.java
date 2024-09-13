package ua.nadiiarubantseva.todo.task;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.nadiiarubantseva.todo.task.dao.Task;
import ua.nadiiarubantseva.todo.task.dao.TaskService;
import ua.nadiiarubantseva.todo.task.dto.*;

import java.security.Principal;
import java.util.List;

@RequestMapping("api/v1/todo")
@RestController
@Tag(name = "Todo Controller", description = "Manages user todo list")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskValidator taskValidator;

    @Operation(summary = "Get todo list", description = "Allows user to list tasks in in todo list")
    @ApiResponse(responseCode = "200", description = "Successful todos list retrieval or validation error",
            content = @Content(schema = @Schema(implementation = GetToDoListResponse.class))
    )
    @GetMapping
    public ResponseEntity<GetToDoListResponse> getTodoList(Principal principal) {
        List<Task> tasks = taskService.getTasksByEmail(principal.getName());
        return ResponseEntity.ok(GetToDoListResponse.success(tasks));
    }

    @Operation(summary = "Add a new task", description = "Create a new task for the user")
    @ApiResponse(responseCode = "200", description = "Task successfully created or validation error",
            content = @Content(schema = @Schema(implementation = CreateTaskResponse.class))
    )
    @PostMapping(value = "/task", consumes = {"multipart/form-data"})
    public ResponseEntity<CreateTaskResponse> createTask(
            @RequestPart("request") CreateTaskRequest request,
            @RequestPart(value = "attachment", required = false) MultipartFile attachment,
            Principal principal
    ) throws Exception {
        CreateTaskResponse.Error error = taskValidator.validate(request);
        if (error != CreateTaskResponse.Error.ok) {
            return ResponseEntity.ok(CreateTaskResponse.fail(error));
        }
        Task createdTask = taskService.createTask(request, principal.getName(), attachment);
        return ResponseEntity.ok(CreateTaskResponse.success(createdTask));
    }

    @Operation(summary = "Update an existing task", description = "Update the details of an existing task")
    @ApiResponse(responseCode = "200", description = "Task successfully updated or validation error",
            content = @Content(schema = @Schema(implementation = UpdateTaskResponse.class))
    )
    @PutMapping("/task/{id}")
    public ResponseEntity<UpdateTaskResponse> updateTask(
            @PathVariable Long id,
            @RequestBody UpdateTaskRequest request,
            Principal principal
    ) {
        UpdateTaskResponse.Error error = taskValidator.validate(request, id, principal.getName());
        if (error != UpdateTaskResponse.Error.ok) {
            return ResponseEntity.ok(UpdateTaskResponse.fail(error));
        }
        taskService.updateTask(id, request);
        return ResponseEntity.ok(UpdateTaskResponse.success());
    }

    @Operation(summary = "Get task", description = "Get the task by id")
    @ApiResponse(responseCode = "200", description = "Task successfully retrieved or validation error",
            content = @Content(schema = @Schema(implementation = GetTaskResponse.class))
    )
    @GetMapping("/task/{id}")
    public ResponseEntity<GetTaskResponse> getTask(
            @PathVariable Long id,
            Principal principal
    ) {
        if (taskValidator.isTaskDoesNotBelongToUser(principal.getName(), id)) {
            return ResponseEntity.ok(GetTaskResponse.fail(GetTaskResponse.Error.invalidTaskId));
        }
        Task task = taskService.findTaskById(id).orElseThrow();
        return ResponseEntity.ok(GetTaskResponse.success(task));
    }

    @Operation(summary = "Delete task", description = "Delete the task by id")
    @ApiResponse(responseCode = "200", description = "Task successfully deleted or validation error",
            content = @Content(schema = @Schema(implementation = DeleteTaskResponse.class))
    )
    @DeleteMapping("/task/{id}")
    public ResponseEntity<DeleteTaskResponse> deleteTask(
            @PathVariable Long id,
            Principal principal
    ) {
        if (taskValidator.isTaskDoesNotBelongToUser(principal.getName(), id)) {
            return ResponseEntity.ok(DeleteTaskResponse.fail(DeleteTaskResponse.Error.invalidTaskId));
        }
        taskService.deleteTaskById(id);
        return ResponseEntity.ok(DeleteTaskResponse.success());
    }
}
