package com.teamhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WorkspaceRequest {

    @NotBlank(message = "워크스페이스 이름은 필수입니다")
    private String name;

    private String description;
}
