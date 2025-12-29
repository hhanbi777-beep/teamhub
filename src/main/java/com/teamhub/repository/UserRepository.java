package com.teamhub.repository;

import com.teamhub.enums.user.AuthProvider;
import com.teamhub.domain.user.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    boolean existsByEmail(String email);

    // 특정시간 이후 가입자 수
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt > :dateTime")
    Long countByCreatedAtAfter(@Param("dateTime") LocalDateTime dateTime);

    // 일별 가입자 (네이티브쿼리)
    @Query(value = "SELECT DATE(created_at) as signup_date, COUNT(*) as signup_count "
        + "FROM users WHERE created_at >= :startDate "
        + "GROUP BY DATE(created_at) ORDER BY signup_date DESC",
    nativeQuery = true)
    List<Object[]> countDailySignups(@Param("startDate") LocalDateTime startDate);

    // 최근 가입 사용자
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findRecentUsers(Pageable pageable);
}
