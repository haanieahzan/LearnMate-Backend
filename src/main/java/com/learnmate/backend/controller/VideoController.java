package com.learnmate.backend.controller;

import com.learnmate.backend.dto.VideoResult;
import com.learnmate.backend.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final YoutubeService youtubeService;

    @GetMapping("/search")
    public ResponseEntity<List<VideoResult>> search(@RequestParam String query) {
        return ResponseEntity.ok(youtubeService.search(query, 3));
    }
}