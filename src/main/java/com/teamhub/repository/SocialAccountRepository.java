package com.teamhub.repository;

import com.teamhub.domain.user.SocialAccount;
import com.teamhub.enums.user.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
    Optional<SocialAccount> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
