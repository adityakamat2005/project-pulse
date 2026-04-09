package com.projectpulse.repository;

import com.projectpulse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(String roleName);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_LEADER' OR r.name = 'ROLE_MEMBER'")
    List<User> findAllLeadersAndMembers();
}
