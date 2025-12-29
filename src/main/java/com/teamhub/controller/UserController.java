package com.teamhub.controller;

import com.teamhub.domain.user.User;
import com.teamhub.dto.response.ApiResponse;
import com.teamhub.dto.response.AuthResponse;
import com.teamhub.enums.ErrorCode;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ApiResponse<AuthResponse.UserInfo> getMyInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_ACCESS_DENIED));

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .build();

        return ApiResponse.success(userInfo);

    }
}
