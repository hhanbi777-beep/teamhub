package com.teamhub.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjectSearchRequest {

    private String keyword;  // 프로젝트명, 설명 검색

}
