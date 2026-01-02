package com.teamhub.dto.response;

import com.teamhub.domain.project.ChecklistItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChecklistItemResponse {

    private Long id;
    private String content;
    private Boolean isCompleted;
    private Integer displayOrder;

    public static ChecklistItemResponse of(ChecklistItem item) {
        return ChecklistItemResponse.builder()
                .id(item.getId())
                .content(item.getContent())
                .isCompleted(item.getIsCompleted())
                .displayOrder(item.getDisplayOrder())
                .build();
    }
}