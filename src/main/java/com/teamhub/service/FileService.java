package com.teamhub.service;

import com.teamhub.config.FileProperties;
import com.teamhub.domain.file.FileAttachment;
import com.teamhub.domain.project.Task;
import com.teamhub.domain.user.User;
import com.teamhub.domain.workspace.WorkspaceMember;
import com.teamhub.dto.response.FileResponse;
import com.teamhub.enums.ErrorCode;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.FileAttachmentRepository;
import com.teamhub.repository.TaskRepository;
import com.teamhub.repository.UserRepository;
import com.teamhub.repository.WorkspaceMemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileProperties fileProperties;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(fileProperties.getUploadDir()).toAbsolutePath().normalize();
        try{
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다", e);
        }
    }

    @Transactional
    public FileResponse uploadFile(Long userId, Long taskId, MultipartFile file) {
        Task task = findTaskById(taskId);
        Long workspaceId = task.getProject().getWorkspace().getId();

        WorkspaceMember member = findMemberOrThrow(workspaceId, userId);

        if(!member.canEditTasks()) {
            throw new CustomException(ErrorCode.TASK_UPDATE_DENIED);
        }

        validateFile(file);

        User uploader = findUserById(userId);
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        String storedName = UUID.randomUUID().toString() + "." + extension;

        try {
            Path targetPath = uploadPath.resolve(storedName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            FileAttachment attachment = FileAttachment.builder()
                    .originalName(originalName)
                    .storedName(storedName)
                    .filePath(targetPath.toString())
                    .fileType(extension)
                    .fileSize(file.getSize())
                    .task(task)
                    .uploader(uploader)
                    .build();

            fileAttachmentRepository.save(attachment);

            log.info("File uploaded: {} for task: {}", originalName, taskId);

            return FileResponse.of(attachment);

        } catch (IOException e) {
            log.error("File upload failed: {}", e.getMessage());
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getFile(Long userId, Long taskId) {
        Task task = findTaskById(taskId);
        Long workspaceId = task.getProject().getWorkspace().getId();

        findMemberOrThrow(workspaceId, userId);

        return fileAttachmentRepository.findAllByTaskId(taskId)
                .stream()
                .map(FileResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Resource downloadFile(Long userId, Long fileId) {
        FileAttachment file = findFileById(fileId);
        Long workspaceId = file.getTask().getProject().getWorkspace().getId();

        findMemberOrThrow(workspaceId, userId);

        try {
            Path filePath = Paths.get(file.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if(resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new CustomException(ErrorCode.FILE_NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    @Transactional
    public void deleteFile(Long userId, Long fileId) {
        FileAttachment file = findFileById(fileId);
        Long workspaceId = file.getTask().getProject().getWorkspace().getId();

        WorkspaceMember member = findMemberOrThrow(workspaceId, userId);

        //본인이 업로드했거나 관리자 이상만 삭제 가능
        if(!file.getUploader().getId().equals(userId) && !member.canManageMembers()) {
            throw new CustomException(ErrorCode.TASK_DELETE_DENIED);
        }

        try {
            Path filePath = Paths.get(file.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("File deleted: {}", e.getMessage());
        }

        fileAttachmentRepository.delete(file);
        log.info("File deleted: {}", fileId);
    }

    //helper methods
    public String getOriginalFileName(Long fileId) {
        return findFileById(fileId).getOriginalName();
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        if(file.getSize() > fileProperties.getMaxSize()) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String extention = getExtension(file.getOriginalFilename());
        if(!fileProperties.getAllowedTypeList().contains(extention.toLowerCase())) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));
    }

    private FileAttachment findFileById(Long fileId) {
        return fileAttachmentRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
    }

    private WorkspaceMember findMemberOrThrow(Long workspaceId, Long userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_ACCESS_DENIED));
    }
}
