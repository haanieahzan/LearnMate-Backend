package com.learnmate.backend.dto;

public record VideoResult(
        String videoId,
        String title,
        String channelTitle,
        String thumbnailUrl,
        String watchUrl
) {}