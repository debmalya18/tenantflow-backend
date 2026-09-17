package com.debmalya.teamtasks_backend.controller;

import com.debmalya.teamtasks_backend.dto.TaskResponse;
import com.debmalya.teamtasks_backend.model.Task;
import com.debmalya.teamtasks_backend.model.User;
import com.debmalya.teamtasks_backend.repository.TaskRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public TaskController(TaskRepository taskRepository, SimpMessagingTemplate messagingTemplate) {
        this.taskRepository = taskRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public TaskResponse createTask(@AuthenticationPrincipal User currentUser, @RequestBody Map<String, String> body) {
        Task task = new Task();
        task.setTitle(body.get("title"));
        task.setTeam(currentUser.getTeam());
        task.setAssignedTo(currentUser);
        Task saved = taskRepository.save(task);

        TaskResponse response = new TaskResponse(saved);
        messagingTemplate.convertAndSend("/topic/team/" + currentUser.getTeam().getId(), response);

        return response;
    }

    @GetMapping
    public List<TaskResponse> getMyTeamTasks(@AuthenticationPrincipal User currentUser) {
        return taskRepository.findByTeam(currentUser.getTeam())
                .stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@AuthenticationPrincipal User currentUser, @PathVariable Long id, @RequestBody Map<String, String> body) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getTeam().getId().equals(currentUser.getTeam().getId())) {
            throw new RuntimeException("This task does not belong to your team");
        }

        if (body.get("title") != null) {
            task.setTitle(body.get("title"));
        }
        if (body.get("status") != null) {
            task.setStatus(body.get("status"));
        }

        Task updated = taskRepository.save(task);
        TaskResponse response = new TaskResponse(updated);
        messagingTemplate.convertAndSend("/topic/team/" + currentUser.getTeam().getId(), response);

        return response;
    }

    @DeleteMapping("/{id}")
    public String deleteTask(@AuthenticationPrincipal User currentUser, @PathVariable Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getTeam().getId().equals(currentUser.getTeam().getId())) {
            throw new RuntimeException("This task does not belong to your team");
        }

        taskRepository.delete(task);
        messagingTemplate.convertAndSend("/topic/team/" + currentUser.getTeam().getId(), "Task " + id + " deleted");

        return "Task deleted successfully";
    }
}
