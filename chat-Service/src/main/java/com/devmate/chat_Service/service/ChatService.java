package com.devmate.chat_Service.service;

import com.devmate.chat_Service.dto.ChatMessageDto;
import com.devmate.chat_Service.dto.ChatRoomDto;
import com.devmate.chat_Service.entity.ChatMessage;
import com.devmate.chat_Service.entity.ChatRoom;
import com.devmate.chat_Service.mapper.ChatMapper;
import com.devmate.chat_Service.repository.ChatMessageRepository;
import com.devmate.chat_Service.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMapper chatMapper;

    @Transactional
    public ChatRoomDto createChatRoom(ChatRoomDto chatRoomDto) {
        log.info("Creating chat room: {}", chatRoomDto.getName());

        // Check if private room already exists between participants
        if (chatRoomDto.getType() == ChatRoom.ChatRoomType.PRIVATE &&
                chatRoomDto.getParticipants().size() == 2) {
            Optional<ChatRoom> existingRoom = chatRoomRepository
                    .findPrivateRoomByParticipants(chatRoomDto.getParticipants());
            if (existingRoom.isPresent()) {
                return chatMapper.toDto(existingRoom.get());
            }
        }

        ChatRoom chatRoom = chatMapper.toEntity(chatRoomDto);
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        log.info("Chat room created successfully with ID: {}", savedRoom.getId());
        return chatMapper.toDto(savedRoom);
    }

    @Transactional
    public ChatMessageDto sendMessage(ChatMessageDto messageDto) {
        log.info("Sending message to room: {}", messageDto.getChatRoomId());

        // Validate chat room exists and user is participant
        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        if (!chatRoom.getParticipants().contains(messageDto.getSenderId())) {
            throw new RuntimeException("User is not a participant in this chat room");
        }

        ChatMessage message = chatMapper.toEntity(messageDto);
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Update room's last activity
        chatRoom.setUpdatedAt(LocalDateTime.now());
        chatRoomRepository.save(chatRoom);

        log.info("Message sent successfully with ID: {}", savedMessage.getId());
        return chatMapper.toDto(savedMessage);
    }

    public List<ChatRoomDto> getUserChatRooms(String userId) {
        return chatRoomRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(chatMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<ChatRoomDto> getChatRoom(String chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .map(chatMapper::toDto);
    }

    public Page<ChatMessageDto> getChatMessages(String chatRoomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chatMessageRepository.findByChatRoomIdAndIsDeletedFalseOrderByTimestampDesc(chatRoomId, pageable)
                .map(chatMapper::toDto);
    }

    public List<ChatMessageDto> getRecentMessages(String chatRoomId, LocalDateTime since) {
        return chatMessageRepository.findRecentMessages(chatRoomId, since)
                .stream()
                .map(chatMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ChatMessageDto> searchMessages(String chatRoomId, String searchTerm) {
        return chatMessageRepository.searchMessagesInRoom(chatRoomId, searchTerm)
                .stream()
                .map(chatMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatRoomDto addParticipant(String chatRoomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        chatRoom.getParticipants().add(userId);
        ChatRoom updatedRoom = chatRoomRepository.save(chatRoom);

        log.info("Added participant {} to chat room {}", userId, chatRoomId);
        return chatMapper.toDto(updatedRoom);
    }

    @Transactional
    public ChatRoomDto removeParticipant(String chatRoomId, String userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        chatRoom.getParticipants().remove(userId);
        ChatRoom updatedRoom = chatRoomRepository.save(chatRoom);

        log.info("Removed participant {} from chat room {}", userId, chatRoomId);
        return chatMapper.toDto(updatedRoom);
    }

    @Transactional
    public ChatMessageDto editMessage(String messageId, String newContent) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.setContent(newContent);
        message.setEdited(true);
        message.setEditedAt(LocalDateTime.now());

        ChatMessage updatedMessage = chatMessageRepository.save(message);
        log.info("Message {} edited successfully", messageId);

        return chatMapper.toDto(updatedMessage);
    }

    @Transactional
    public void deleteMessage(String messageId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.setDeleted(true);
        chatMessageRepository.save(message);

        log.info("Message {} deleted successfully", messageId);
    }

    public Long getUnreadMessageCount(String chatRoomId, LocalDateTime lastReadTime) {
        return chatMessageRepository.countUnreadMessages(chatRoomId, lastReadTime);
    }

    public List<ChatRoomDto> searchChatRooms(String searchTerm, String userId) {
        return chatRoomRepository.searchRooms(searchTerm, userId)
                .stream()
                .map(chatMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatRoomDto createProjectChatRoom(String projectId, String projectName, String createdBy, Set<String> participants) {
        // Check if project room already exists
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByProjectIdAndIsActiveTrue(projectId);
        if (existingRoom.isPresent()) {
            return chatMapper.toDto(existingRoom.get());
        }

        ChatRoom projectRoom = ChatRoom.builder()
                .name(projectName + " - Project Chat")
                .description("Chat room for project: " + projectName)
                .type(ChatRoom.ChatRoomType.PROJECT)
                .participants(participants)
                .createdBy(createdBy)
                .projectId(projectId)
                .isActive(true)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(projectRoom);
        log.info("Project chat room created for project: {}", projectId);

        return chatMapper.toDto(savedRoom);
    }
}
