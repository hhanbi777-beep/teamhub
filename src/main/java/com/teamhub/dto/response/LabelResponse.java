package com.teamhub.dto.response;

import com.teamhub.domain.project.Label;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LabelResponse {

    private Long id;
    private String name;
    private String color;

    public static LabelResponse of(Label label) {
        return LabelResponse.builder()
                .id(label.getId())
                .name(label.getName())
                .color(label.getColor())
                .build();
    }
}