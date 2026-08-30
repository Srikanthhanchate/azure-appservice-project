package com.orginsight.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Stores uploaded knowledge-base documents on local disk under
 * {upload.dir}/knowledge/. Files are served back via the static resource
 * mapping configured in WebConfig (/uploads/**).
 *
 * For a multi-instance/cloud deployment, swap this out for S3/GCS-backed
 * storage - the KnowledgeItemService only depends on this interface's
 * store()/publicUrlFor() contract, so callers won't need to change.
 */
@Service
public class FileStorageService {

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024; // 20MB, matches application.properties

    public StoredFile store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was provided.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds the maximum allowed size of 20MB.");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
        String storedName = UUID.randomUUID() + extension;

        Path targetDir = Paths.get(uploadDir, "knowledge");
        Files.createDirectories(targetDir);

        Path targetPath = targetDir.resolve(storedName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        String publicUrl = "/uploads/knowledge/" + storedName;
        return new StoredFile(originalName, publicUrl);
    }

    public record StoredFile(String originalFileName, String url) {
    }
}
