package com.devmate.chat_Service.dto;

import com.devmate.chat_Service.entity.ChatRoom;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
    
    private String id;

    @NotBlank(message = "Room name is required")
    @Size(max = 100, message = "Room name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private ChatRoom.ChatRoomType type;
    private Set<String> participants;
    private String createdBy;
    private boolean isActive;
    private String projectId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional fields for response
    private ChatMessageDto lastMessage;
    private int unreadCount;
    private int participantCount;
}
