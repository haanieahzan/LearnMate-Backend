package com.learnmate.backend.dto;

import java.util.List;

public record LecturerAnalyticsResponse(
        int totalCourses,
        int totalStudents,
        List<CourseAnalytics> courses
) {}