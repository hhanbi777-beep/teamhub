package com.teamhub.enums.activity;

public enum ActivityType {
    //Workspace
    WORKSPACE_CREATED,
    WORKSPACE_UPDATED,
    WORKSPACE_DELETED,
    MEMBER_INVITED,
    MEMBER_REMOVED,

    //Project
    PROJECT_CREATED,
    PROJECT_UPDATED,
    PROJECT_DELETED,

    //Task
    TASK_CREATED,
    TASK_UPDATED,
    TASK_DELETED,
    TASK_STATUS_CHANGED,
    TASK_ASSIGNED,

    //Comment
    COMMENT_ADDED,
    COMMENT_UPDATED,
    COMMENT_DELETED
}
