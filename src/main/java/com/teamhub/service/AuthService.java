package com.teamhub.service;

import com.teamhub.domain.user.PasswordResetToken;
import com.teamhub.domain.user.RefreshToken;
import com.teamhub.domain.user.User;
import com.teamhub.dto.request.*;
import com.teamhub.dto.response.AuthResponse;
import com.teamhub.enums.ErrorCode;
import com.teamhub.enums.user.AuthProvider;
import com.teamhub.enums.user.UserRole;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.PasswordResetTokenRepository;
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
    private final LoginAttemptService loginAttemptService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;


    @Transactional
    public AuthResponse signUp(SignUpRequest req) {
        //이메일 중복체크
        if(userRepository.existsByEmail(req.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
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

        String email = req.getEmail();

        //계정 잠금 확인
        if (loginAttemptService.isBlocked(email)) {
            throw new CustomException(ErrorCode.ACCOUNT_LOCKED);
        }

        //사용자 조회
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        //비밀번호 확인
        if(!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        log.info("User login: {}", user.getEmail());

        //토큰 생성 및 반환
        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequest req) {
        //리프레시 토큰 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(req.getRefreshToken())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        //만료 체크
        if(refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
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
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        log.info("Password changed for user: {}", userId);
    }

    @Transactional
    public String requestPasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 기존 토큰 무효화
        PasswordResetToken token = PasswordResetToken.create(user, 30); // 30분 유효
        passwordResetTokenRepository.save(token);

        // TODO: 이메일 발송 (나중에 구현)
        log.info("Password reset token created for user: {}", user.getEmail());

        return token.getToken(); // 개발용으로 토큰 반환 (실제로는 이메일로만)
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        if (!token.isValid()) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = token.getUser();
        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        token.markAsUsed();

        // 로그인 시도 횟수 초기화
        loginAttemptService.loginSucceeded(user.getEmail());

        log.info("Password reset completed for user: {}", user.getEmail());
    }
}
