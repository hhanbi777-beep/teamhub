package com.teamhub.controller;

import com.teamhub.dto.request.LoginRequest;
import com.teamhub.dto.request.SignUpRequest;
import com.teamhub.dto.request.TokenRefreshRequest;
import com.teamhub.dto.response.ApiResponse;
import com.teamhub.dto.response.AuthResponse;
import com.teamhub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignUpRequest request) {
        AuthResponse response = authService.signUp(request);
        return ApiResponse.success("회원가입 성공", response);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ApiResponse.success("로그인 성공", response);
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ApiResponse.success("토큰 갱신 성공", response);
    }

    @PostMapping("/logout")
    public ApiResponse<AuthResponse> logout(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        authService.logout(userId);
        return ApiResponse.success("로그아웃 성공", null);
    }
}
