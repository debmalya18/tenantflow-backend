package com.debmalya.teamtasks_backend.repository;

import com.debmalya.teamtasks_backend.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}