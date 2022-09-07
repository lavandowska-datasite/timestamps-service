package com.datasite.timestamps.model;

import org.springframework.util.StringUtils;

public class RequestContext {

    private String userId;
    private String projectId;
    private String taskListId;
    private String taskId;

    public String getUserId() {
        return userId;
    }

    public RequestContext setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getProjectId() {
        return projectId;
    }

    public RequestContext setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getTaskListId() {
        return taskListId;
    }

    public RequestContext setTaskListId(String taskListId) {
        this.taskListId = taskListId;
        return this;
    }

    public String getTaskId() {
        return taskId;
    }

    public RequestContext setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public String asKey() {
        String userKey = StringUtils.hasLength(userId) ? "userId_" : "";
        String projectKey = StringUtils.hasLength(projectId) ? "projectId_" : "";
        String taskListKey = StringUtils.hasLength(taskListId) ? "taskListId_" : "";
        String taskKey = StringUtils.hasLength(taskId) ? "taskId_" : "";
        return userKey + projectKey + taskListKey + taskKey;
    }

    public String asValue() {
        String userValue = StringUtils.hasLength(userId) ? userId : "";
        String projectValue = StringUtils.hasLength(projectId) ? projectId : "";
        String taskListValue = StringUtils.hasLength(taskListId) ? taskListId : "";
        String taskValue = StringUtils.hasLength(taskId) ? taskId : "";
        return userValue + projectValue + taskListValue + taskValue;
    }

    @Override
    public String toString() {
        return "RequestContext{" +
            "userId='" + userId + '\'' +
            ", projectId='" + projectId + '\'' +
            ", taskListId='" + taskListId + '\'' +
            ", taskId='" + taskId + '\'' +
            '}';
    }
}
