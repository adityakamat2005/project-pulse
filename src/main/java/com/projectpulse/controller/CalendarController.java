package com.projectpulse.controller;

import com.projectpulse.entity.Project;
import com.projectpulse.entity.Task;
import com.projectpulse.entity.User;
import com.projectpulse.service.ProjectService;
import com.projectpulse.service.TaskService;
import com.projectpulse.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String calendarView(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long projectId,
            Model model) {

        User currentUser = securityUtils.getCurrentUser();

        // Default to current month
        LocalDate today = LocalDate.now();
        int displayYear  = (year  != null) ? year  : today.getYear();
        int displayMonth = (month != null) ? month : today.getMonthValue();

        YearMonth yearMonth = YearMonth.of(displayYear, displayMonth);
        LocalDate firstDay  = yearMonth.atDay(1);
        LocalDate lastDay   = yearMonth.atEndOfMonth();

        // Fetch tasks for the month
        List<Task> tasks;
        if (projectId != null) {
            Project project = projectService.findById(projectId);
            tasks = taskService.findByProject(project).stream()
                .filter(t -> t.getDeadline() != null
                    && !t.getDeadline().isBefore(firstDay)
                    && !t.getDeadline().isAfter(lastDay))
                .collect(Collectors.toList());
        } else if (currentUser.hasRole("ROLE_MEMBER")) {
            tasks = taskService.findAssignedToUser(currentUser).stream()
                .filter(t -> t.getDeadline() != null
                    && !t.getDeadline().isBefore(firstDay)
                    && !t.getDeadline().isAfter(lastDay))
                .collect(Collectors.toList());
        } else {
            // Leader / Admin: show tasks from all their projects
            tasks = projectService.findProjectsForUser(currentUser).stream()
                .flatMap(p -> taskService.findByProject(p).stream())
                .filter(t -> t.getDeadline() != null
                    && !t.getDeadline().isBefore(firstDay)
                    && !t.getDeadline().isAfter(lastDay))
                .distinct()
                .collect(Collectors.toList());
        }

        // Group tasks by day number for easy rendering in template
        Map<Integer, List<Task>> tasksByDay = tasks.stream()
            .collect(Collectors.groupingBy(t -> t.getDeadline().getDayOfMonth()));

        // Build calendar grid (weeks × days)
        // firstDay.getDayOfWeek().getValue() -> 1=Mon ... 7=Sun; we want 0=Sun offset
        int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; // 0=Sun
        List<LocalDate> calendarDays = new ArrayList<>();
        // pad start
        for (int i = 0; i < startDayOfWeek; i++) calendarDays.add(null);
        for (int d = 1; d <= lastDay.getDayOfMonth(); d++) calendarDays.add(LocalDate.of(displayYear, displayMonth, d));
        // pad end to full weeks
        while (calendarDays.size() % 7 != 0) calendarDays.add(null);

        // Navigation
        YearMonth prevMonth = yearMonth.minusMonths(1);
        YearMonth nextMonth = yearMonth.plusMonths(1);

        model.addAttribute("currentUser",   currentUser);
        model.addAttribute("yearMonth",     yearMonth);
        model.addAttribute("today",         today);
        model.addAttribute("calendarDays",  calendarDays);
        model.addAttribute("tasksByDay",    tasksByDay);
        model.addAttribute("prevYear",      prevMonth.getYear());
        model.addAttribute("prevMonth",     prevMonth.getMonthValue());
        model.addAttribute("nextYear",      nextMonth.getYear());
        model.addAttribute("nextMonth",     nextMonth.getMonthValue());
        model.addAttribute("projects",      projectService.findProjectsForUser(currentUser));
        model.addAttribute("selectedProject", projectId);

        return "calendar/view";
    }
}
