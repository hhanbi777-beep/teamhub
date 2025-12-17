package com.teamhub.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력입니다"),

    // Auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다. 다시 로그인해주세요"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),

    // Workspace
    WORKSPACE_NOT_FOUND(HttpStatus.NOT_FOUND, "워크스페이스를 찾을 수 없습니다"),
    WORKSPACE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "워크스페이스 접근 권한이 없습니다"),
    WORKSPACE_UPDATE_DENIED(HttpStatus.FORBIDDEN, "워크스페이스 수정 권한이 없습니다"),
    WORKSPACE_DELETE_DENIED(HttpStatus.FORBIDDEN, "워크스페이스 삭제는 소유자만 가능합니다"),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "멤버를 찾을 수 없습니다"),
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 워크스페이스 멤버입니다"),
    MEMBER_INVITE_DENIED(HttpStatus.FORBIDDEN, "멤버 초대 권한이 없습니다"),
    MEMBER_REMOVE_DENIED(HttpStatus.FORBIDDEN, "멤버 제거 권한이 없습니다"),
    CANNOT_ASSIGN_OWNER(HttpStatus.BAD_REQUEST, "OWNER 역할은 부여할 수 없습니다"),
    CANNOT_REMOVE_OWNER(HttpStatus.BAD_REQUEST, "소유자는 제거할 수 없습니다"),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이메일의 사용자를 찾을 수 없습니다"),

    // Project
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다"),
    PROJECT_CREATE_DENIED(HttpStatus.FORBIDDEN, "프로젝트 생성 권한이 없습니다"),
    PROJECT_UPDATE_DENIED(HttpStatus.FORBIDDEN, "프로젝트 수정 권한이 없습니다"),
    PROJECT_DELETE_DENIED(HttpStatus.FORBIDDEN, "프로젝트 삭제 권한이 없습니다"),

    // Task
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "태스크를 찾을 수 없습니다"),
    TASK_CREATE_DENIED(HttpStatus.FORBIDDEN, "태스크 생성 권한이 없습니다"),
    TASK_UPDATE_DENIED(HttpStatus.FORBIDDEN, "태스크 수정 권한이 없습니다"),
    TASK_DELETE_DENIED(HttpStatus.FORBIDDEN, "태스크 삭제 권한이 없습니다"),
    TASK_STATUS_CHANGE_DENIED(HttpStatus.FORBIDDEN, "태스크 상태 변경 권한이 없습니다"),

    // Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다"),
    COMMENT_CREATE_DENIED(HttpStatus.FORBIDDEN, "댓글 작성 권한이 없습니다"),
    COMMENT_UPDATE_DENIED(HttpStatus.FORBIDDEN, "본인의 댓글만 수정할 수 있습니다"),
    COMMENT_DELETE_DENIED(HttpStatus.FORBIDDEN, "댓글 삭제 권한이 없습니다"),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다"),
    NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "알림 접근 권한이 없습니다"),

    // File
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다"),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 제한을 초과했습니다"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "허용되지 않는 파일 형식입니다"),
    FILE_DELETE_DENIED(HttpStatus.FORBIDDEN, "파일 삭제 권한이 없습니다");

    private final HttpStatus status;
    private final String message;
}
