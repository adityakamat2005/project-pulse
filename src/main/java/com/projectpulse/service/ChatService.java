package com.projectpulse.service;

import com.projectpulse.dto.ChatMessageDto;
import com.projectpulse.entity.ChatMessage;
import com.projectpulse.entity.Project;
import com.projectpulse.entity.User;
import com.projectpulse.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ProjectService projectService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessagesForProject(Project project) {
        return chatMessageRepository.findByProjectOrderBySentAtAsc(project);
    }

    public ChatMessage sendMessage(Long projectId, String content, User sender) {
        Project project = projectService.findById(projectId);

        ChatMessage message = new ChatMessage();
        message.setContent(content);
        message.setSender(sender);
        message.setProject(project);
        message.setType(ChatMessage.MessageType.TEXT);

        return chatMessageRepository.save(message);
    }

    /**
     * WebSocket handler: save and return DTO for broadcast
     */
    public ChatMessageDto handleWebSocketMessage(ChatMessageDto dto) {
        Project project = projectService.findById(dto.getProjectId());
        User sender = userService.findByUsername(dto.getSenderUsername());

        ChatMessage saved = sendMessage(project.getId(), dto.getContent(), sender);

        ChatMessageDto response = new ChatMessageDto();
        response.setId(saved.getId());
        response.setContent(saved.getContent());
        response.setSenderUsername(sender.getUsername());
        response.setSenderFullName(sender.getFullName());
        response.setProjectId(project.getId());
        response.setSentAt(saved.getSentAt());
        response.setType(saved.getType().name());
        return response;
    }

    /**
     * Convert entity to DTO for Thymeleaf rendering
     */
    public ChatMessageDto toDto(ChatMessage message) {
        return new ChatMessageDto(
            message.getId(),
            message.getContent(),
            message.getSender().getUsername(),
            message.getSender().getFullName(),
            message.getProject().getId(),
            message.getSentAt(),
            message.getType().name()
        );
    }

    public List<ChatMessageDto> getDtoMessages(Project project) {
        return getMessagesForProject(project).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}
