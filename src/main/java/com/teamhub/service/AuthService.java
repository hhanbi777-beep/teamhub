package com.teamhub.service;

import com.teamhub.domain.user.RefreshToken;
import com.teamhub.domain.user.User;
import com.teamhub.dto.request.LoginRequest;
import com.teamhub.dto.request.SignUpRequest;
import com.teamhub.dto.request.TokenRefreshRequest;
import com.teamhub.dto.response.AuthResponse;
import com.teamhub.enums.user.AuthProvider;
import com.teamhub.enums.user.UserRole;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.RefreshTokenRepository;
import com.teamhub.repository.UserRepository;
import com.teamhub.security.JwtProperties;
import com.teamhub.security.JwtTokenProvider;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
                .provider(AuthProvider.LOCAL)
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        //토큰 생성 및 반환
        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        //사용자 조회
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new CustomException("이메일 또는 비밀번호가 올바르지 않습니다", HttpStatus.UNAUTHORIZED));

        //비밀번호 확인
        if(!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new CustomException("이메일 또는 비밀번호가 올바르지 않습니다", HttpStatus.UNAUTHORIZED);
        }

        log.info("User login: {}", user.getEmail());

        //토큰 생성 및 반환
        return createAuthResponse(user);
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

        //기존 리프레시 토큰 삭제 후 새로발급
        refreshTokenRepository.delete(refreshToken);

        log.info("Refresh token registered: {}", user.getEmail());

        return createAuthResponse(user);
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        refreshTokenRepository.deleteByUser(user);
        log.info("User logout: {}", user.getEmail());
    }

    private AuthResponse createAuthResponse(User user) {
        //엑세스 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());

        //리프레시 토큰 생성 및 저장
        String refreshToeknStr = jwtTokenProvider.createRefreshToken(user.getId());

        //기존 리프레시토큰 삭제
        refreshTokenRepository.deleteByUser(user);

        //새 리프레시토큰 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshToeknStr)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenValidity() / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);


        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToeknStr)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .profileImage(user.getProfileImage())
                        .build())
                .build();
    }
}
