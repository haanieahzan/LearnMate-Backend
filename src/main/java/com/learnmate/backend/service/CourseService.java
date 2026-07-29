package com.learnmate.backend.service;

import com.learnmate.backend.dto.CourseResponse;
import com.learnmate.backend.dto.CreateCourseRequest;
import com.learnmate.backend.model.*;
import com.learnmate.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final FieldRepository fieldRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final LearningResourceRepository resourceRepository;
    private final AnnouncementRepository announcementRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final DocumentChunkRepository documentChunkRepository;

    public CourseResponse createCourse(CreateCourseRequest request, User lecturer) {
        Course course = new Course();
        course.setCode(request.code());
        course.setTitle(request.title());
        course.setLecturer(lecturer);

        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
            course.setDepartment(department);
        }
        if (request.fieldId() != null) {
            Field field = fieldRepository.findById(request.fieldId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Field not found"));
            course.setField(field);
        }

        courseRepository.save(course);
        return toResponse(course);
    }

    public List<CourseResponse> listCourses() {
        return courseRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CourseResponse getCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        return toResponse(course);
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCode(),
                course.getTitle(),
                course.getLecturer().getFullName(),
                course.getDepartment() != null ? course.getDepartment().getName() : null,
                course.getCreatedAt()
        );
    }
    @org.springframework.transaction.annotation.Transactional
    public void deleteCourse(UUID courseId, User actingUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        boolean isOwner = course.getLecturer().getId().equals(actingUser.getId());
        boolean isAdmin = actingUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own courses");
        }

        // 1. Quizzes: attempts and questions first, then the quizzes themselves
        List<Quiz> quizzes = quizRepository.findByCourseId(courseId);
        List<UUID> quizIds = quizzes.stream().map(Quiz::getId).toList();
        if (!quizIds.isEmpty()) {
            quizAttemptRepository.deleteAll(quizAttemptRepository.findByQuizIdIn(quizIds));
            quizQuestionRepository.deleteAll(quizQuestionRepository.findByQuizIdIn(quizIds));
        }
        quizRepository.deleteAll(quizzes);

        // 2. Resources: chunks + files on disk, then the resource rows
        //    (document_chunks deletion added once DocumentChunk is confirmed)
        // 2. Resources: chunks first (they reference resource_id), then the
        //    actual file on disk, then the resource rows themselves.
        List<LearningResource> resources = resourceRepository.findByCourseId(courseId);
        for (LearningResource r : resources) {
            documentChunkRepository.deleteAll(documentChunkRepository.findByResourceId(r.getId()));
            try {
                java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(r.getFilePath()));
            } catch (Exception ignored) {
                // best-effort — a missing file shouldn't block course deletion
            }
        }
        resourceRepository.deleteAll(resources);

        // 3. Announcements and enrollments
        announcementRepository.deleteAll(announcementRepository.findByCourseIdOrderByCreatedAtDesc(courseId));
        enrollmentRepository.deleteAll(enrollmentRepository.findByCourseId(courseId));

        // 4. The course itself, last
        courseRepository.delete(course);
    }
}