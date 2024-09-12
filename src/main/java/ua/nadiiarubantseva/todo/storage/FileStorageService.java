package ua.nadiiarubantseva.todo.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String uploadFile(MultipartFile multipartFile, String userEmail, Long taskId) throws Exception;
}
