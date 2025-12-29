package com.teamhub.dto.response;

import com.teamhub.domain.workspace.WorkspaceMember;
import com.teamhub.enums.workspace.WorkspaceRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MemberResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String profileImage;
    private WorkspaceRole role;

    public static MemberResponse of(WorkspaceMember member) {
        return MemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .name(member.getUser().getName())
                .email(member.getUser().getEmail())
                .profileImage(member.getUser().getProfileImage())
                .role(member.getRole())
                .build();
    }
}
