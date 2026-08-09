package com.debmalya.teamtasks_backend.repository;

import com.debmalya.teamtasks_backend.model.Task;
import com.debmalya.teamtasks_backend.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByTeam(Team team);
}