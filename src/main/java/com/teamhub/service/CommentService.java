package com.teamhub.service;

import com.teamhub.domain.project.Comment;
import com.teamhub.domain.project.Task;
import com.teamhub.domain.user.User;
import com.teamhub.domain.workspace.WorkspaceMember;
import com.teamhub.dto.request.CommentRequest;
import com.teamhub.dto.response.CommentResponse;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.CommentRepository;
import com.teamhub.repository.TaskRepository;
import com.teamhub.repository.UserRepository;
import com.teamhub.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponse createComment(Long userId, Long taskId, CommentRequest req) {
        Task task = findTaskById(taskId);
        Long workspaceId = task.getProject().getWorkspace().getId();

        WorkspaceMember member = findMemberOrThrow(workspaceId, userId);

        User author = findUserById(userId);

        Comment comment = Comment.builder()
                .content(req.getContent())
                .task(task)
                .author(author)
                .build();

        commentRepository.save(comment);

        //활동 로그 기록
        activityLogService.log(
                task.getProject().getWorkspace(),
                author,
                "COMMENT_ADDED",
                "TASK",
                task.getId(),
                task.getTitle(),
                "댓글: " + truncate(req.getContent(), 50)
        );

        //테스크 담당자에게 알림(본인제외)
        if(task.getAssignee() != null && !task.getAssignee().getId().equals(userId)) {
            notificationService.sendCommentNotification(task, author, req.getContent());
        }

        log.info("Comment created on task: {} by user: {}", taskId, userId);

        return CommentResponse.of(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long userId, Long taskId) {
        Task task = findTaskById(taskId);
        Long workspaceId = task.getProject().getWorkspace().getId();

        findMemberOrThrow(workspaceId, userId);

        return commentRepository.findAllByTaskId(taskId)
                .stream()
                .map(CommentResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse updateComment(Long userId, Long commentId, CommentResponse req) {
        Comment comment = findCommentById(commentId);

        if(!comment.getAuthor().getId().equals(userId)) {
            throw new CustomException("본인의 댓글만 수정할 수 있습니다", HttpStatus.FORBIDDEN);
        }

        comment.updateContent(req.getContent());

        return CommentResponse.of(comment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = findCommentById(commentId);
        Long workspaceId = comment.getTask().getProject().getWorkspace().getId();

        WorkspaceMember member = findMemberOrThrow(workspaceId, userId);

        //본인 댓글이거나 관리자 이상만 삭제 가능
        if(!comment.getAuthor().getId().equals(userId) && !member.canManageMembers()) {
            throw new CustomException("댓글 삭제 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        commentRepository.delete(comment);
        log.info("Comment deleted: {}", commentId);
    }

    //helper methods
    private String truncate(String text, int maxLength) {
        if(text == null) return null;
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException("테스크를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException("댓글을 찾을 수 없습니다", HttpStatus.NOT_FOUND));
    }

    private WorkspaceMember findMemberOrThrow(Long workspaceId, Long userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new CustomException("워크스페이스 접근 권한이 없습니다", HttpStatus.FORBIDDEN));
    }
}
