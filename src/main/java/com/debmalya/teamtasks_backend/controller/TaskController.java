package com.debmalya.teamtasks_backend.controller;

import com.debmalya.teamtasks_backend.dto.TaskResponse;
import com.debmalya.teamtasks_backend.model.Task;
import com.debmalya.teamtasks_backend.model.User;
import com.debmalya.teamtasks_backend.repository.TaskRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @PostMapping
    public TaskResponse createTask(@AuthenticationPrincipal User currentUser, @RequestBody Map<String, String> body) {
        Task task = new Task();
        task.setTitle(body.get("title"));
        task.setTeam(currentUser.getTeam());
        task.setAssignedTo(currentUser);
        Task saved = taskRepository.save(task);
        return new TaskResponse(saved);
    }

    @GetMapping
    public List<TaskResponse> getMyTeamTasks(@AuthenticationPrincipal User currentUser) {
        return taskRepository.findByTeam(currentUser.getTeam())
                .stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }
}
