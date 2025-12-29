package com.teamhub.controller;

import com.teamhub.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/public/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("TeamHub API is running");
    }
}
