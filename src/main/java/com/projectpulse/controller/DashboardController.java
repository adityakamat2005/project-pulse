package com.projectpulse.controller;

import com.projectpulse.entity.Project;
import com.projectpulse.entity.Task;
import com.projectpulse.entity.User;
import com.projectpulse.service.*;
import com.projectpulse.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final SecurityUtils securityUtils;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final UserService userService;
    private final SubmissionService submissionService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        User currentUser = securityUtils.getCurrentUser();
        model.addAttribute("currentUser", currentUser);

        List<Project> userProjects = projectService.findProjectsForUser(currentUser);
        model.addAttribute("projects", userProjects);

        if (currentUser.hasRole("ROLE_ADMIN")) {
            buildAdminDashboard(model);
        } else if (currentUser.hasRole("ROLE_LEADER")) {
            buildLeaderDashboard(model, currentUser, userProjects);
        } else {
            buildMemberDashboard(model, currentUser);
        }

        // Upcoming deadlines next 7 days — project is eagerly loaded now
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        List<Task> allMyTasks = taskService.findAssignedToUser(currentUser);
        List<Task> upcomingTasks = allMyTasks.stream()
                .filter(t -> t.getDeadline() != null
                        && !t.getDeadline().isBefore(today)
                        && !t.getDeadline().isAfter(nextWeek)
                        && t.getStatus() != Task.TaskStatus.APPROVED)
                .collect(Collectors.toList());
        model.addAttribute("upcomingTasks", upcomingTasks);

        return "dashboard";
    }

    private void buildAdminDashboard(Model model) {
        model.addAttribute("totalUsers",       userService.countAll());
        model.addAttribute("totalProjects",    projectService.countAll());
        model.addAttribute("totalTasks",       taskService.countAll());
        model.addAttribute("completedTasks",   taskService.countCompleted());
        model.addAttribute("pendingApprovals", taskService.countPendingApprovals());
        model.addAttribute("activeProjects",   projectService.countActive());
        model.addAttribute("recentProjects",   projectService.findAll()
                .stream().limit(5).collect(Collectors.toList()));
    }

    private void buildLeaderDashboard(Model model, User currentUser, List<Project> projects) {
        model.addAttribute("pendingApprovals", submissionService.countPendingForLeader(currentUser));
        model.addAttribute("totalProjects",    projects.size());

        long totalTasks     = projects.stream().mapToLong(p -> p.getTasks().size()).sum();
        long completedTasks = projects.stream().mapToLong(Project::getCompletedTaskCount).sum();
        model.addAttribute("totalTasks",     totalTasks);
        model.addAttribute("completedTasks", completedTasks);

        List<Task> pendingSubTasks = submissionService.findPendingForLeader(currentUser)
                .stream().map(s -> s.getTask()).distinct().collect(Collectors.toList());
        model.addAttribute("pendingSubmissionTasks", pendingSubTasks);
    }

    private void buildMemberDashboard(Model model, User currentUser) {
        List<Task> myTasks = taskService.findActiveTasksForUser(currentUser);
        model.addAttribute("myTasks",        myTasks);
        model.addAttribute("totalMyTasks",   myTasks.size());
        model.addAttribute("submittedCount", taskService.countSubmittedByUser(currentUser));

        long inProgressCount = myTasks.stream()
                .filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS).count();
        model.addAttribute("inProgressCount", inProgressCount);
    }
}