package com.teamhub.enums.workspace;

public enum WorkspaceRole {
    OWNER,  //워크스페이스 소유자(모든 권한)
    ADMIN,  //관리자 (멤버관리, 프로젝트 관리)
    MEMBER, //일반 멤버(테스크 생성, 수정)
    VIEWER  //읽기 전용
}
