package com.teamhub.domain.user;

import com.teamhub.domain.common.BaseEntity;
import com.teamhub.enums.user.AuthProvider;
import com.teamhub.enums.user.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(nullable = false)
    private String name;

    private String profileImage;

    private String providerId;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    public void updateProfile(String name, String profileImage) {
        this.name=name;
        this.profileImage=profileImage;
    }
}
