package com.learnmate.backend.repository;

import com.learnmate.backend.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findByLecturerId(UUID lecturerId);
}