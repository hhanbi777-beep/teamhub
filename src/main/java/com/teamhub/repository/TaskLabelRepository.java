package com.teamhub.repository;

import com.teamhub.domain.project.TaskLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskLabelRepository extends JpaRepository<TaskLabel, Long> {
    List<TaskLabel> findAllByTaskId(Long taskId);
    void deleteByTaskIdAndLabelId(Long taskId, Long labelId);
}