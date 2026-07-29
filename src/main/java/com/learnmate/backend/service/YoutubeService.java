package com.learnmate.backend.service;

import com.learnmate.backend.dto.VideoResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class YoutubeService {

    private final RestClient restClient;
    private final String apiKey;

    public YoutubeService(@Value("${youtube.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://www.googleapis.com/youtube/v3")
                .build();
    }

    @SuppressWarnings("unchecked")
    public List<VideoResult> search(String query, int maxResults) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search")
                            .queryParam("part", "snippet")
                            .queryParam("q", query)
                            .queryParam("type", "video")
                            .queryParam("maxResults", maxResults)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(Map.class);

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            List<VideoResult> results = new ArrayList<>();

            for (Map<String, Object> item : items) {
                Map<String, Object> id = (Map<String, Object>) item.get("id");
                Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                Map<String, Object> thumbnails = (Map<String, Object>) snippet.get("thumbnails");
                Map<String, Object> defaultThumb = (Map<String, Object>) thumbnails.get("medium");

                String videoId = (String) id.get("videoId");
                results.add(new VideoResult(
                        videoId,
                        (String) snippet.get("title"),
                        (String) snippet.get("channelTitle"),
                        (String) defaultThumb.get("url"),
                        "https://www.youtube.com/watch?v=" + videoId
                ));
            }
            return results;
        } catch (Exception e) {
            log.error("YouTube search failed for query '{}': {}", query, e.getMessage());
            return List.of(); // fail gracefully — a broken video search shouldn't break the page
        }
    }
}