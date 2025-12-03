package com.teamhub.domain.user;

import com.teamhub.domain.common.BaseEntity;
import com.teamhub.enums.user.AuthProvider;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "social_account")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class SocialAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column
    private AuthProvider provider;

    @Column(nullable = false)
    private String providerId;

    private String accessToken;

    private String refreshToken;
}
