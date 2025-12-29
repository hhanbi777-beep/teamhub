package com.teamhub.domain.project;

import com.teamhub.domain.common.BaseEntity;
import com.teamhub.domain.workspace.Workspace;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "labels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Label extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String color;  // HEX 컬러 코드 (#FF5733)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    public void update(String name, String color) {
        this.name = name;
        this.color = color;
    }
}