package com.teamhub.repository;

import com.teamhub.domain.project.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {
    List<Label> findAllByWorkspaceId(Long workspaceId);
    Optional<Label> findByWorkspaceIdAndName(Long workspaceId, String name);
}