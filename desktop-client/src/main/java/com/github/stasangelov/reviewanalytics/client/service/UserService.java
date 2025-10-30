package com.github.stasangelov.reviewanalytics.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.stasangelov.reviewanalytics.client.model.ErrorResponseDto;
import com.github.stasangelov.reviewanalytics.client.model.UserManagementDto;
import okhttp3.*;

import java.io.IOException;
import java.util.List;

public class UserService {
    private static final String BASE_URL = "http://localhost:8080/api/users";
    private final OkHttpClient client = HttpClientService.getClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public List<UserManagementDto> getAllUsers() throws IOException {
        Request request = new Request.Builder().url(BASE_URL).get().build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) handleError(response);
            return objectMapper.readValue(response.body().string(), new TypeReference<>() {});
        }
    }

    public UserManagementDto changeRole(Long userId, String role) throws IOException {
        String json = "{\"role\":\"" + role + "\"}";
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder().url(BASE_URL + "/" + userId + "/role").patch(body).build();
        try (Response response = client.newCall(request).execute()) {
            return processResponse(response);
        }
    }

    public UserManagementDto changeStatus(Long userId, boolean status) throws IOException {
        String json = "{\"active\":" + status + "}";
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder().url(BASE_URL + "/" + userId + "/status").patch(body).build();
        try (Response response = client.newCall(request).execute()) {
            return processResponse(response);
        }
    }

    private UserManagementDto processResponse(Response response) throws IOException {
        if (response.isSuccessful()) {
            return objectMapper.readValue(response.body().string(), UserManagementDto.class);
        }
        handleError(response);
        return null;
    }

    private void handleError(Response response) throws IOException {
        String errorBody = response.body().string();
        try {
            ErrorResponseDto errorDto = objectMapper.readValue(errorBody, ErrorResponseDto.class);
            throw new ApiException(response.code(), errorDto.getMessage());
        } catch (Exception e) {
            throw new ApiException(response.code(), "Не удалось распознать ошибку: " + errorBody);
        }
    }
}
