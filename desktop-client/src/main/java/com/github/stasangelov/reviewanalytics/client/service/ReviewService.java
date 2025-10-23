package com.github.stasangelov.reviewanalytics.client.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.stasangelov.reviewanalytics.client.model.ReviewDto;
import okhttp3.*;
import java.io.IOException;
import java.util.List;

public class ReviewService {
    private static final String BASE_URL = "http://localhost:8080/api/reviews";
    private final OkHttpClient client = HttpClientService.getClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReviewService() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    public List<ReviewDto> getAllReviews() throws IOException {
        Request request = new Request.Builder().url(BASE_URL).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return objectMapper.readValue(response.body().string(), new TypeReference<>() {});
        }
    }

    public ReviewDto createReview(ReviewDto review) throws IOException {
        String json = objectMapper.writeValueAsString(review);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder().url(BASE_URL).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return objectMapper.readValue(response.body().string(), ReviewDto.class);
        }
    }
}