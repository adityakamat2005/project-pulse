package com.projectpulse.config;

import com.projectpulse.entity.Project;
import com.projectpulse.entity.Role;
import com.projectpulse.entity.Task;
import com.projectpulse.entity.User;
import com.projectpulse.repository.ProjectRepository;
import com.projectpulse.repository.RoleRepository;
import com.projectpulse.repository.TaskRepository;
import com.projectpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // --- Seed Roles ---
        Role adminRole = getOrCreateRole("ROLE_ADMIN");
        Role leaderRole = getOrCreateRole("ROLE_LEADER");
        Role memberRole = getOrCreateRole("ROLE_MEMBER");

        // --- Seed Admin User ---
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setFullName("System Administrator");
            admin.setUsername("admin");
            admin.setEmail("admin@projectpulse.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
            log.info("✅ Admin user created: admin / Admin@123");
        }

        // --- Seed Demo Leader ---
        if (!userRepository.existsByUsername("leader1")) {
            User leader = new User();
            leader.setFullName("Aditya Kumar");
            leader.setUsername("leader1");
            leader.setEmail("leader1@projectpulse.com");
            leader.setPassword(passwordEncoder.encode("Leader@123"));
            leader.setRoles(Set.of(leaderRole));
            userRepository.save(leader);
            log.info("✅ Leader user created: leader1 / Leader@123");
        }

        // --- Seed Demo Members ---
        if (!userRepository.existsByUsername("member1")) {
            User member = new User();
            member.setFullName("Jnanesh Bhat");
            member.setUsername("member1");
            member.setEmail("member1@projectpulse.com");
            member.setPassword(passwordEncoder.encode("Member@123"));
            member.setRoles(Set.of(memberRole));
            userRepository.save(member);
            log.info("✅ Member user created: member1 / Member@123");
        }

        if (!userRepository.existsByUsername("member2")) {
            User member2 = new User();
            member2.setFullName("Kartik Naik");
            member2.setUsername("member2");
            member2.setEmail("member2@projectpulse.com");
            member2.setPassword(passwordEncoder.encode("Member@123"));
            member2.setRoles(Set.of(memberRole));
            userRepository.save(member2);
            log.info("✅ Member user created: member2 / Member@123");
        }

        // --- Seed Demo Project ---
        if (projectRepository.count() == 0) {
            User leader = userRepository.findByUsername("leader1").orElseThrow();
            User member1 = userRepository.findByUsername("member1").orElseThrow();
            User member2 = userRepository.findByUsername("member2").orElseThrow();

            Project project = new Project();
            project.setTitle("IoT-Based Areca Nut Monitoring System");
            project.setDescription("A smart IoT system integrating moisture sensing and image processing for areca nut quality grading.");
            project.setDeadline(LocalDate.now().plusMonths(2));
            project.setStatus(Project.ProjectStatus.ACTIVE);
            project.setCreatedBy(leader);
            project.setLeader(leader);
            project.setMembers(Set.of(member1, member2));
            projectRepository.save(project);

            // Seed Tasks
            createTask("System Architecture Design", "Design overall system architecture and component diagram",
                Task.Priority.HIGH, LocalDate.now().plusDays(7), project, member1, leader);
            createTask("Moisture Sensor Integration", "Integrate capacitive moisture sensor with ESP32 board",
                Task.Priority.HIGH, LocalDate.now().plusDays(14), project, member1, leader);
            createTask("Image Processing Module", "Implement areca nut grading using image processing",
                Task.Priority.MEDIUM, LocalDate.now().plusDays(21), project, member2, leader);
            createTask("Web Dashboard Development", "Build monitoring dashboard with real-time data",
                Task.Priority.MEDIUM, LocalDate.now().plusDays(30), project, member2, leader);
            createTask("Final Testing & Report", "Conduct system integration tests and prepare final report",
                Task.Priority.LOW, LocalDate.now().plusDays(45), project, member1, leader);

            log.info("✅ Demo project and tasks seeded");
        }

        log.info("🚀 ProjectPulse started successfully!");
        log.info("📌 Login credentials: admin/Admin@123 | leader1/Leader@123 | member1/Member@123");
    }

    private Role getOrCreateRole(String name) {
        return roleRepository.findByName(name)
            .orElseGet(() -> {
                Role role = new Role(name);
                return roleRepository.save(role);
            });
    }

    private void createTask(String title, String desc, Task.Priority priority,
                             LocalDate deadline, Project project, User assignedTo, User createdBy) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(desc);
        task.setPriority(priority);
        task.setDeadline(deadline);
        task.setProject(project);
        task.setAssignedTo(assignedTo);
        task.setCreatedBy(createdBy);
        task.setStatus(Task.TaskStatus.TODO);
        taskRepository.save(task);
    }
}
