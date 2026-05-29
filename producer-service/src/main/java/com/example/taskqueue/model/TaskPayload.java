package com.example.taskqueue.model;

import java.io.Serializable;

public class TaskPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    private String taskId;
    private String taskType;
    private int executionTimeMs;

    public TaskPayload() {
    }

    public TaskPayload(String taskId, String taskType, int executionTimeMs) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.executionTimeMs = executionTimeMs;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public int getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(int executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    @Override
    public String toString() {
        return "TaskPayload{" +
                "taskId='" + taskId + '\'' +
                ", taskType='" + taskType + '\'' +
                ", executionTimeMs=" + executionTimeMs +
                '}';
    }
}
