package com.projectpulse.controller;

import com.projectpulse.dto.ChatMessageDto;
import com.projectpulse.entity.Project;
import com.projectpulse.entity.User;
import com.projectpulse.service.ChatService;
import com.projectpulse.service.ProjectService;
import com.projectpulse.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ProjectService projectService;
    private final SecurityUtils securityUtils;
    private final SimpMessagingTemplate messagingTemplate;

    // ── Chat room page ──────────────────────────────────────────────────────
    @GetMapping("/chat/{projectId}")
    public String chatRoom(@PathVariable Long projectId, Model model) {
        User currentUser = securityUtils.getCurrentUser();
        Project project = projectService.findById(projectId);
        List<ChatMessageDto> messages = chatService.getDtoMessages(project);

        model.addAttribute("project", project);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUser", currentUser);
        return "chat/room";
    }

    // ── HTTP fallback: post message ─────────────────────────────────────────
    @PostMapping("/chat/{projectId}/send")
    public String sendMessage(
            @PathVariable Long projectId,
            @RequestParam String content,
            RedirectAttributes redirectAttributes) {

        if (content == null || content.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Message cannot be empty.");
            return "redirect:/chat/" + projectId;
        }

        User currentUser = securityUtils.getCurrentUser();
        chatService.sendMessage(projectId, content.trim(), currentUser);
        return "redirect:/chat/" + projectId;
    }

    // ── WebSocket: receive and broadcast ────────────────────────────────────
    @MessageMapping("/chat.send")
    public void handleWebSocketMessage(@Payload ChatMessageDto dto) {
        ChatMessageDto saved = chatService.handleWebSocketMessage(dto);
        // Broadcast to all subscribers of /topic/chat/{projectId}
        messagingTemplate.convertAndSend(
            "/topic/chat/" + dto.getProjectId(), saved);
    }
}
