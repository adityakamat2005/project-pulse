package com.projectpulse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {

    private Long id;
    private String content;
    private String senderUsername;
    private String senderFullName;
    private Long projectId;
    private LocalDateTime sentAt;
    private String type; // TEXT, SYSTEM
}
