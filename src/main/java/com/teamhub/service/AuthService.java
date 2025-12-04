package com.teamhub.service;

import com.teamhub.domain.user.RefreshToken;
import com.teamhub.domain.user.User;
import com.teamhub.dto.request.SignUpRequest;
import com.teamhub.dto.request.TokenRefreshRequest;
import com.teamhub.dto.response.AuthResponse;
import com.teamhub.enums.user.UserRole;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.RefreshTokenRepository;
import com.teamhub.repository.UserRepository;
import com.teamhub.security.JwtProperties;
import com.teamhub.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponse signUp(SignUpRequest req) {
        //이메일 중복체크
        if(userRepository.existsByEmail(req.getEmail())) {
            throw new CustomException("이미 사용중인 이메일입니다.", HttpStatus.CONFLICT);
        }

        //사용자 생성
        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        //토큰 생성 및 반환
        return createAuthRespose(user);
    }

    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest req) {
        //리프레시 토큰 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(req.getRefreshToken())
                .orElseThrow(() -> new CustomException("유효하지 않은 리프레시 토큰입니다", HttpStatus.UNAUTHORIZED));

        //만료 체크
        if(refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new CustomException("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요", HttpStatus.UNAUTHORIZED);
        }

        User user = refreshToken.getUser();
        return createAuthRespose(user);
    }

    private AuthResponse createAuthRespose(User user) {
        //엑세스 토큰 생성

        return AuthResponse.builder().build();
    }
}
