package com.learnmate.backend.service;

import com.learnmate.backend.dto.LearningResourceResponse;
import com.learnmate.backend.model.Course;
import com.learnmate.backend.model.LearningResource;
import com.learnmate.backend.model.User;
import com.learnmate.backend.repository.CourseRepository;
import com.learnmate.backend.repository.LearningResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import java.net.MalformedURLException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LearningResourceService {

    private final LearningResourceRepository resourceRepository;
    private final CourseRepository courseRepository;
    private final AiServiceClient aiServiceClient;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public LearningResourceResponse uploadResource(UUID courseId, MultipartFile file, User uploader) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        try {
            Path dirPath = Paths.get(uploadDir);
            Files.createDirectories(dirPath);

            String originalName = file.getOriginalFilename();
            String extension = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf('.') + 1)
                    : "bin";

            // Prefix with a random UUID so two different lecturers uploading
            // "notes.pdf" never overwrite each other on disk.
            String storedFilename = UUID.randomUUID() + "." + extension;
            Path targetPath = dirPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetPath);

            LearningResource resource = new LearningResource();
            resource.setCourse(course);
            resource.setUploadedBy(uploader);
            resource.setTitle(originalName != null ? originalName : storedFilename);
            resource.setFilePath(targetPath.toString());
            resource.setFileType(extension.toUpperCase());

            resourceRepository.save(resource);

            // Fire off ingestion into the AI service (extract → chunk → embed →
            // store). Best-effort: if this fails, the upload still succeeds.
            // Send an ABSOLUTE path — the Python service runs from a different
            // working directory and can't resolve Spring Boot's relative paths.
            String absolutePath = targetPath.toAbsolutePath().toString();
            aiServiceClient.ingestResource(resource.getId(), absolutePath);

            return toResponse(resource);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file");
        }
    }

    public List<LearningResourceResponse> listByCourse(UUID courseId) {
        return resourceRepository.findByCourseId(courseId).stream().map(this::toResponse).toList();
    }

    private LearningResourceResponse toResponse(LearningResource r) {
        return new LearningResourceResponse(
                r.getId(), r.getCourse().getId(), r.getTitle(),
                r.getFileType(), r.getUploadedBy().getFullName(), r.getUploadedAt()
        );
    }

    public ResponseEntity<Resource> loadFileForDownload(UUID resourceId) {
        LearningResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));
        try {
            Path path = Paths.get(resource.getFilePath());
            Resource fileResource = new UrlResource(path.toUri());
            if (!fileResource.exists() || !fileResource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found on disk");
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getTitle() + "\"")
                    .body(fileResource);
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read file");
        }
    }

    public void deleteResource(UUID resourceId) {
        LearningResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found"));

        // Best-effort: remove the file from disk too, so deleted uploads don't
        // pile up. If the file's already gone, we don't fail the whole delete.
        try {
            Files.deleteIfExists(Paths.get(resource.getFilePath()));
        } catch (IOException e) {
            // Log-and-continue: the DB row still gets removed below.
        }

        resourceRepository.delete(resource);
    }
}