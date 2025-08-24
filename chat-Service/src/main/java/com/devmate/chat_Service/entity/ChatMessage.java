package com.devmate.chat_Service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_room_timestamp", columnList = "chatRoomId, timestamp"),
    @Index(name = "idx_sender_timestamp", columnList = "senderId, timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String chatRoomId;

    @Column(nullable = false)
    private String senderId;

    @Column(nullable = false, length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageType type = MessageType.TEXT;

    private String replyToMessageId;

    private String metadata; // JSON string for additional data

    @Builder.Default
    private boolean isEdited = false;

    @Builder.Default
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    private LocalDateTime editedAt;

    public enum MessageType {
        TEXT, IMAGE, FILE, CODE, SYSTEM, AI_GENERATED
    }
}
