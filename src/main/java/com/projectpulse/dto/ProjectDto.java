package com.projectpulse.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class ProjectDto {

    private Long id;

    @NotBlank(message = "Project title is required")
    @Size(min = 2, max = 200, message = "Title must be 2-200 characters")
    private String title;

    @Size(max = 2000, message = "Description too long")
    private String description;

    @NotNull(message = "Deadline is required")
    @Future(message = "Deadline must be a future date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deadline;

    @NotNull(message = "Please assign a project leader")
    private Long leaderId;

    private List<Long> memberIds;

    private String status;
}
