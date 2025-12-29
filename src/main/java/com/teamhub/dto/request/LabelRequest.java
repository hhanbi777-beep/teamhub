package com.teamhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LabelRequest {

    @NotBlank(message = "라벨 이름은 필수입니다")
    private String name;

    @NotBlank(message = "색상은 필수입니다")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "올바른 HEX 색상 코드가 아닙니다")
    private String color;
}