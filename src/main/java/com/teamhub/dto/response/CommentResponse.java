package com.teamhub.dto.response;

import com.teamhub.domain.project.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String content;
    private Long taskId;
    private AuthorInfo author;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String name;
        private String profileImage;
    }

    public static CommentResponse of(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .taskId(comment.getId())
                .author(AuthorInfo.builder()
                        .id(comment.getId())
                        .name(comment.getAuthor().getName())
                        .profileImage(comment.getAuthor().getProfileImage())
                        .build())
                .createAt(comment.getCreatedAt())
                .updateAt(comment.getUpdatedAt())
                .build();
    }
}
