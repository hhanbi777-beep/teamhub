package com.teamhub.repository;

import com.teamhub.domain.project.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.task.id = :taskId ORDER BY c.createdAt ASC")
    List<Comment> findAllByTaskId(@Param("taskId") Long taskId);

    Long countByTaskId(Long taskId);
}
