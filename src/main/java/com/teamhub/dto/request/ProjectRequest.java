package com.teamhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProjectRequest {

    @NotBlank(message = "프로젝트 이름은 필수입니다.")
    private String name;

    private String description;
}
