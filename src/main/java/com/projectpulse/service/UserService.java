package com.projectpulse.service;

import com.projectpulse.dto.UserRegistrationDto;
import com.projectpulse.entity.Role;
import com.projectpulse.entity.User;
import com.projectpulse.exception.ResourceNotFoundException;
import com.projectpulse.repository.RoleRepository;
import com.projectpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // --- Read ---

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> findByRole(String roleName) {
        return userRepository.findByRoleName(roleName);
    }

    @Transactional(readOnly = true)
    public List<User> findAllLeadersAndMembers() {
        return userRepository.findAllLeadersAndMembers();
    }

    // --- Create ---

    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + dto.getUsername());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }
        if (!dto.isPasswordMatching()) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String roleName = (dto.getRole() != null && !dto.getRole().isBlank())
            ? dto.getRole() : "ROLE_MEMBER";

        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRoles(Set.of(role));

        return userRepository.save(user);
    }

    // --- Update ---

    public User updateProfile(Long id, String fullName, String bio, String phone) {
        User user = findById(id);
        user.setFullName(fullName);
        user.setBio(bio);
        user.setPhone(phone);
        return userRepository.save(user);
    }

    public User changePassword(Long id, String currentPassword, String newPassword) {
        User user = findById(id);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public User updateRole(Long userId, String roleName) {
        User user = findById(userId);
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
        user.setRoles(Set.of(role));
        return userRepository.save(user);
    }

    public void toggleUserStatus(Long userId) {
        User user = findById(userId);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    // --- Stats ---

    @Transactional(readOnly = true)
    public long countAll() {
        return userRepository.count();
    }
}
