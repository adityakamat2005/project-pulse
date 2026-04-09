package com.projectpulse.repository;

import com.projectpulse.entity.Project;
import com.projectpulse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.leader LEFT JOIN FETCH p.members LEFT JOIN FETCH p.createdBy")
    List<Project> findAll();

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.leader LEFT JOIN FETCH p.members LEFT JOIN FETCH p.createdBy WHERE p.id = :id")
    Optional<Project> findById(Long id);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.leader LEFT JOIN FETCH p.members LEFT JOIN FETCH p.createdBy WHERE p.leader = :leader")
    List<Project> findByLeader(User leader);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.leader LEFT JOIN FETCH p.members LEFT JOIN FETCH p.createdBy WHERE p.createdBy = :user")
    List<Project> findByCreatedBy(User user);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.leader LEFT JOIN FETCH p.members LEFT JOIN FETCH p.createdBy WHERE :user MEMBER OF p.members")
    List<Project> findByMember(User user);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.leader LEFT JOIN FETCH p.members LEFT JOIN FETCH p.createdBy WHERE p.leader = :user OR :user MEMBER OF p.members")
    List<Project> findByLeaderOrMember(User user);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = 'ACTIVE'")
    long countActiveProjects();
}