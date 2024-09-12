package ua.nadiiarubantseva.todo.task.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ua.nadiiarubantseva.todo.attachment.Attachment;
import ua.nadiiarubantseva.todo.attachment.AttachmentRepository;
import ua.nadiiarubantseva.todo.storage.FileStorageService;
import ua.nadiiarubantseva.todo.task.dto.CreateTaskRequest;
import ua.nadiiarubantseva.todo.task.dto.UpdateTaskRequest;
import ua.nadiiarubantseva.todo.user.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    public Optional<Task> findTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public void deleteTaskById(Long id) {
        taskRepository.deleteById(id);
    }

    public List<Task> getTasksByEmail(String email) {
        return taskRepository.findAllByUserEmailOrderByCreatedAtAsc(email);
    }

    @Transactional
    public Task createTask(CreateTaskRequest request, String userEmail, MultipartFile multipartFile) throws Exception {
        Task task = Task.builder()
                .user(userRepository.findByEmail(userEmail).orElseThrow())
                .description(request.getDescription())
                .build();
        task = taskRepository.save(task);

        if (multipartFile != null && !multipartFile.isEmpty()) {
            Attachment attachment = saveFile(userEmail, multipartFile, task);
            task.setAttachment(attachment);
            taskRepository.save(task);
        }
        return task;
    }

    private Attachment saveFile(String userEmail, MultipartFile multipartFile, Task task) throws Exception {
        String fileLink = fileStorageService.uploadFile(multipartFile, userEmail, task.getId());
        Attachment attachment = Attachment.builder()
                .fileName(multipartFile.getName())
                .fileLink(fileLink)
                .build();
        return attachmentRepository.save(attachment);
    }

    public void updateTask(Long id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id).orElseThrow();
        task.setDescription(request.getDescription());
        task.setComplete(request.getComplete());
        taskRepository.save(task);
    }
}
