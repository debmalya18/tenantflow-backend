package com.debmalya.teamtasks_backend.dto;

import com.debmalya.teamtasks_backend.model.Task;

public class TaskResponse {

    private Long id;
    private String title;
    private String status;
    private String teamName;
    private String assignedToName;

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.status = task.getStatus();
        this.teamName = task.getTeam().getName();
        this.assignedToName = task.getAssignedTo().getName();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public String getTeamName() {
        return teamName;
    }

    public String getAssignedToName() {
        return assignedToName;
    }
}
