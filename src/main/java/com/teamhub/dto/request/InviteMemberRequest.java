package com.teamhub.dto.request;

import com.teamhub.enums.workspace.WorkspaceRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InviteMemberRequest {

    @NotBlank(message = "이메일은 필수 입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "역할은 필수입니다.")
    private WorkspaceRole role;
}
