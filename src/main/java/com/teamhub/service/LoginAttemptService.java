package com.teamhub.service;

import com.teamhub.domain.user.LoginAttempt;
import com.teamhub.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 30;

    private final LoginAttemptRepository loginAttemptRepository;

    @Transactional
    public void loginFailed(String email) {
        LoginAttempt attempt = loginAttemptRepository.findByEmail(email)
                .orElseGet(() -> LoginAttempt.builder()
                        .email(email)
                        .attemptCount(0)
                        .build());

        attempt.incrementAttempt();

        if (attempt.getAttemptCount() >= MAX_ATTEMPTS) {
            attempt.lock(LOCK_MINUTES);
            log.warn("Account locked for email: {} for {} minutes", email, LOCK_MINUTES);
        }

        loginAttemptRepository.save(attempt);
    }

    @Transactional
    public void loginSucceeded(String email) {
        loginAttemptRepository.findByEmail(email)
                .ifPresent(attempt -> {
                    attempt.reset();
                    loginAttemptRepository.save(attempt);
                });
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(String email) {
        return loginAttemptRepository.findByEmail(email)
                .map(LoginAttempt::isLocked)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public int getRemainingAttempts(String email) {
        return loginAttemptRepository.findByEmail(email)
                .map(attempt -> MAX_ATTEMPTS - attempt.getAttemptCount())
                .orElse(MAX_ATTEMPTS);
    }
}