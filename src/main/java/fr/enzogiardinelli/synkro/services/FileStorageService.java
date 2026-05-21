package fr.enzogiardinelli.synkro.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path storageLocation = Paths.get("uploads/avatars");

    public FileStorageService() {
        try {
            Files.createDirectories(storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

    public String storeFile(MultipartFile file, String customFileName) {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }

        try {
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";

            String finalFileName = customFileName + fileExtension;

            Path targetLocation = this.storageLocation.resolve(finalFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/avatars/" + finalFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl != null && !fileUrl.isEmpty()) {
            try {
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                Path filePath = this.storageLocation.resolve(fileName).normalize();

                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete file", e);
            }
        }
    }
}
