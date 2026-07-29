package com.learnmate.backend.repository;

import com.learnmate.backend.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
    List<Enrollment> findByCourseId(UUID courseId);
}