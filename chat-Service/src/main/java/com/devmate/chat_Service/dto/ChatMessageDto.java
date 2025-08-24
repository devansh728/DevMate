package com.devmate.chat_Service.dto;

import com.devmate.chat_Service.entity.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    
    private String id;

    @NotBlank(message = "Chat room ID is required")
    private String chatRoomId;

    @NotBlank(message = "Sender ID is required")
    private String senderId;

    @NotBlank(message = "Content is required")
    @Size(max = 4000, message = "Content must not exceed 4000 characters")
    private String content;

    @NotNull
    private ChatMessage.MessageType type;

    private String replyToMessageId;
    private String metadata;
    private boolean isEdited;
    private boolean isDeleted;
    private LocalDateTime timestamp;
    private LocalDateTime editedAt;
    private String senderName;
    private String senderAvatarUrl;
}
