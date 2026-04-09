package com.projectpulse.repository;

import com.projectpulse.entity.ChatMessage;
import com.projectpulse.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByProjectOrderBySentAtAsc(Project project);

    List<ChatMessage> findTop50ByProjectOrderBySentAtDesc(Project project);
}
