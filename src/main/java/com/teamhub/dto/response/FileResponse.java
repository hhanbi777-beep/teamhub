package com.teamhub.dto.response;

import com.teamhub.domain.file.FileAttachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class FileResponse {

    private Long id;
    private String originalName;
    private String fileType;
    private Long fileSize;
    private String fileSizeFormatted;
    private Long taskId;
    private UploaderInfo uploader;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UploaderInfo{
        private Long id;
        private String name;
    }

    public static FileResponse of(FileAttachment file) {
        return FileResponse.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .fileSizeFormatted(formatFileSize(file.getFileSize()))
                .taskId(file.getTask().getId())
                .uploader(UploaderInfo.builder()
                        .id(file.getUploader().getId())
                        .name(file.getUploader().getName())
                        .build())
                .createdAt(file.getCreatedAt())
                .build();
    }

    private static String formatFileSize(Long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / 1024.0 / 1024.0);
        }
    }
}
