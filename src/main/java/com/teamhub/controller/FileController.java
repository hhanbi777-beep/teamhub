package com.teamhub.controller;

import com.teamhub.dto.response.ApiResponse;
import com.teamhub.dto.response.FileResponse;
import com.teamhub.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ApiResponse<FileResponse> uploadFile(Authentication authentication,
                                                @RequestParam Long taskId,
                                                @RequestParam("file") MultipartFile file) {
        Long userId = (Long) authentication.getPrincipal();
        FileResponse res = fileService.uploadFile(userId, taskId, file);
        return ApiResponse.success("파일 업로드 성공", res);
    }

    @GetMapping("/list")
    public ApiResponse<List<FileResponse>> getFiles(Authentication authentication,
                                                    @RequestParam Long taskId) {
        Long userId = (Long) authentication.getPrincipal();
        List<FileResponse> res = fileService.getFile(userId, taskId);
        return ApiResponse.success(res);
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(Authentication authentication,
                                                 @RequestParam Long fileId) {
        Long userId = (Long) authentication.getPrincipal();
        Resource resource = fileService.downloadFile(userId, fileId);
        String filename = fileService.getOriginalFileName(fileId);
        String encodedFilename= URLEncoder.encode(filename, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .body(resource);
    }

    @PostMapping("/delete")
    public ApiResponse<Void> deleteFile(Authentication authentication,
                                        @RequestParam Long fileId) {
        Long userId = (Long) authentication.getPrincipal();
        fileService.deleteFile(userId, fileId);
        return ApiResponse.success("파일 삭제 성공", null);
    }
}
