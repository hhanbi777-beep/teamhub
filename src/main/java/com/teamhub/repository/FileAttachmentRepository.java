package com.teamhub.repository;

import com.teamhub.domain.file.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    @Query("SELECT f FROM FileAttachment f JOIN FETCH f.uploader WHERE f.task.id = :taskId ORDER BY f.createdAt DESC")
    List<FileAttachment> findAllByTaskId(@Param("taskId") Long taskId);

    Long countByTaskId(Long taskId);
}
