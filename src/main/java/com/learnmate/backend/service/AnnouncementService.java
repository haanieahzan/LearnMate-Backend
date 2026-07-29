package com.learnmate.backend.service;

import com.learnmate.backend.dto.AnnouncementResponse;
import com.learnmate.backend.dto.CreateAnnouncementRequest;
import com.learnmate.backend.model.Announcement;
import com.learnmate.backend.model.Course;
import com.learnmate.backend.model.User;
import com.learnmate.backend.repository.AnnouncementRepository;
import com.learnmate.backend.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final CourseRepository courseRepository;

    public AnnouncementResponse create(UUID courseId, CreateAnnouncementRequest request, User poster) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        Announcement announcement = new Announcement();
        announcement.setCourse(course);
        announcement.setPostedBy(poster);
        announcement.setTitle(request.title());
        announcement.setContent(request.content());

        announcementRepository.save(announcement);
        return toResponse(announcement);
    }

    public List<AnnouncementResponse> listByCourse(UUID courseId) {
        return announcementRepository.findByCourseIdOrderByCreatedAtDesc(courseId)
                .stream().map(this::toResponse).toList();
    }

    public void delete(UUID announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Announcement not found"));
        announcementRepository.delete(announcement);
    }

    private AnnouncementResponse toResponse(Announcement a) {
        return new AnnouncementResponse(
                a.getId(), a.getCourse().getId(), a.getTitle(),
                a.getContent(), a.getPostedBy().getFullName(), a.getCreatedAt()
        );
    }
}