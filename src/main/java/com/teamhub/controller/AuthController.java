package com.teamhub.controller;

import com.teamhub.dto.request.*;
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

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        authService.changePassword(userId, request);
        return ApiResponse.success("비밀번호가 변경되었습니다", null);
    }

    @PostMapping("/password-reset/request")
    public ApiResponse<String> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        String token = authService.requestPasswordReset(request);
        return ApiResponse.success("비밀번호 재설정 이메일이 발송되었습니다", token);
    }

    @PostMapping("/password-reset/confirm")
    public ApiResponse<Void> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return ApiResponse.success("비밀번호가 재설정되었습니다", null);
    }
}
