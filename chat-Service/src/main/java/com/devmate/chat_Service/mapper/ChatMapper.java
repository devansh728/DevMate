package com.devmate.chat_Service.mapper;


import com.devmate.chat_Service.dto.ChatMessageDto;
import com.devmate.chat_Service.dto.ChatRoomDto;
import com.devmate.chat_Service.entity.ChatMessage;
import com.devmate.chat_Service.entity.ChatRoom;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    public ChatRoomDto toDto(ChatRoom chatRoom) {
        if (chatRoom == null) {
            return null;
        }

        return ChatRoomDto.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .description(chatRoom.getDescription())
                .type(chatRoom.getType())
                .participants(chatRoom.getParticipants())
                .createdBy(chatRoom.getCreatedBy())
                .isActive(chatRoom.isActive())
                .projectId(chatRoom.getProjectId())
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .participantCount(chatRoom.getParticipants() != null ? chatRoom.getParticipants().size() : 0)
                .build();
    }

    public ChatRoom toEntity(ChatRoomDto chatRoomDto) {
        if (chatRoomDto == null) {
            return null;
        }

        return ChatRoom.builder()
                .id(chatRoomDto.getId())
                .name(chatRoomDto.getName())
                .description(chatRoomDto.getDescription())
                .type(chatRoomDto.getType())
                .participants(chatRoomDto.getParticipants())
                .createdBy(chatRoomDto.getCreatedBy())
                .isActive(chatRoomDto.isActive())
                .projectId(chatRoomDto.getProjectId())
                .build();
    }

    public ChatMessageDto toDto(ChatMessage chatMessage) {
        if (chatMessage == null) {
            return null;
        }

        return ChatMessageDto.builder()
                .id(chatMessage.getId())
                .chatRoomId(chatMessage.getChatRoomId())
                .senderId(chatMessage.getSenderId())
                .content(chatMessage.getContent())
                .type(chatMessage.getType())
                .replyToMessageId(chatMessage.getReplyToMessageId())
                .metadata(chatMessage.getMetadata())
                .isEdited(chatMessage.isEdited())
                .isDeleted(chatMessage.isDeleted())
                .timestamp(chatMessage.getTimestamp())
                .editedAt(chatMessage.getEditedAt())
                .build();
    }

    public ChatMessage toEntity(ChatMessageDto chatMessageDto) {
        if (chatMessageDto == null) {
            return null;
        }

        return ChatMessage.builder()
                .id(chatMessageDto.getId())
                .chatRoomId(chatMessageDto.getChatRoomId())
                .senderId(chatMessageDto.getSenderId())
                .content(chatMessageDto.getContent())
                .type(chatMessageDto.getType())
                .replyToMessageId(chatMessageDto.getReplyToMessageId())
                .metadata(chatMessageDto.getMetadata())
                .isEdited(chatMessageDto.isEdited())
                .isDeleted(chatMessageDto.isDeleted())
                .build();
    }
}
