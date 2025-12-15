package com.teamhub.controller;

import com.teamhub.dto.request.CommentRequest;
import com.teamhub.dto.response.ApiResponse;
import com.teamhub.dto.response.CommentResponse;
import com.teamhub.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ApiResponse<CommentResponse> createComment(Authentication authentication, @RequestParam Long taskId,
                                                      @Valid @RequestBody CommentRequest req) {
    Long userId = (Long) authentication.getPrincipal();
    CommentResponse res = commentService.createComment(userId, taskId, req);
    return ApiResponse.success("댓글 작성 성공", res);
    }

    @GetMapping("/list")
    public ApiResponse<List<CommentResponse>> getComments(Authentication authentication, @RequestParam Long taskId) {
        Long userId = (Long) authentication.getPrincipal();
        List<CommentResponse> res = commentService.getComments(userId, taskId);
        return ApiResponse.success(res);
    }

    @PostMapping("/delete")
    public ApiResponse<CommentResponse> updateComment(Authentication authentication,
                                                      @RequestParam Long commentId) {
        Long userId = (Long) authentication.getPrincipal();
        commentService.deleteComment(userId, commentId);
        return ApiResponse.success("댓글 삭제 성공", null);
    }
}
