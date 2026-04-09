package com.projectpulse.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class TaskDto {

    private Long id;

    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 200, message = "Title must be 2-200 characters")
    private String title;

    @Size(max = 2000, message = "Description too long")
    private String description;

    @NotNull(message = "Deadline is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;

    @NotBlank(message = "Priority is required")
    private String priority; // LOW, MEDIUM, HIGH

    private Long assignedToId;

    private Long projectId;

    private boolean pinned;
}
