package ua.nadiiarubantseva.todo.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class DumpStorageService implements FileStorageService{

    public static final String FILE_NAME_FORMAT = "cloud_provider_host/bucket/%s/%d/%s";

    @Override
    public String uploadFile(MultipartFile multipartFile, String userEmail, Long taskId) throws Exception {
        try {
            byte[] bytes = multipartFile.getBytes();
            // todo: upload file
        } catch (Exception e) {
            log.error("Error occurred during file upload", e);
            throw e;
        }
        return String.format(FILE_NAME_FORMAT, userEmail, taskId, multipartFile.getName()) ;
    }
}
