package com.debmalya.teamtasks_backend.controller;

import com.debmalya.teamtasks_backend.dto.CreateTeamRequest;
import com.debmalya.teamtasks_backend.model.Team;
import com.debmalya.teamtasks_backend.model.User;
import com.debmalya.teamtasks_backend.repository.TeamRepository;
import com.debmalya.teamtasks_backend.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamController(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public Team createTeam(@AuthenticationPrincipal User currentUser, @RequestBody CreateTeamRequest request) {
        Team team = new Team();
        team.setName(request.getName());
        Team savedTeam = teamRepository.save(team);

        currentUser.setTeam(savedTeam);
        currentUser.setRole("LEADER");
        userRepository.save(currentUser);

        return savedTeam;
    }

    @PostMapping("/members/{userId}")
    public String addMember(@AuthenticationPrincipal User currentUser, @PathVariable Long userId) {

        if (!currentUser.getRole().equals("LEADER")) {
            throw new RuntimeException("Only team leaders can add members");
        }

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userToAdd.setTeam(currentUser.getTeam());
        userRepository.save(userToAdd);

        return "Member added successfully";
    }

    @DeleteMapping("/members/{userId}")
    public String removeMember(@AuthenticationPrincipal User currentUser, @PathVariable Long userId) {

        if (!currentUser.getRole().equals("LEADER")) {
            throw new RuntimeException("Only team leaders can remove members");
        }

        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!userToRemove.getTeam().getId().equals(currentUser.getTeam().getId())) {
            throw new RuntimeException("This user is not in your team");
        }

        userToRemove.setTeam(null);
        userRepository.save(userToRemove);

        return "Member removed successfully";
    }

    @PostMapping("/members/{userId}/promote")
    public String promoteToLeader(@AuthenticationPrincipal User currentUser, @PathVariable Long userId) {

        if (!currentUser.getRole().equals("LEADER")) {
            throw new RuntimeException("Only team leaders can promote members");
        }

        User userToPromote = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!userToPromote.getTeam().getId().equals(currentUser.getTeam().getId())) {
            throw new RuntimeException("This user is not in your team");
        }

        userToPromote.setRole("LEADER");
        userRepository.save(userToPromote);

        return "Member promoted to leader";
    }
}
