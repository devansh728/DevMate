package com.devmate.chat_Service.Controller;

import com.devmate.chat_Service.dto.BaseResponse;
import com.devmate.chat_Service.dto.ChatMessageDto;
import com.devmate.chat_Service.dto.ChatRoomDto;
import com.devmate.chat_Service.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public ResponseEntity<BaseResponse<ChatRoomDto>> createChatRoom(@Valid @RequestBody ChatRoomDto chatRoomDto) {
        try {
            ChatRoomDto createdRoom = chatService.createChatRoom(chatRoomDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success("Chat room created successfully", createdRoom));
        } catch (Exception e) {
            log.error("Error creating chat room: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/rooms/user/{userId}")
    public ResponseEntity<BaseResponse<List<ChatRoomDto>>> getUserChatRooms(@PathVariable String userId) {
        List<ChatRoomDto> chatRooms = chatService.getUserChatRooms(userId);
        return ResponseEntity.ok(BaseResponse.success(chatRooms));
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<BaseResponse<ChatRoomDto>> getChatRoom(@PathVariable String roomId) {
        return chatService.getChatRoom(roomId)
                .map(room -> ResponseEntity.ok(BaseResponse.success(room)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/rooms/{roomId}/participants/{userId}")
    public ResponseEntity<BaseResponse<ChatRoomDto>> addParticipant(
            @PathVariable String roomId,
            @PathVariable String userId) {
        try {
            ChatRoomDto updatedRoom = chatService.addParticipant(roomId, userId);
            return ResponseEntity.ok(BaseResponse.success("Participant added successfully", updatedRoom));
        } catch (Exception e) {
            log.error("Error adding participant: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/rooms/{roomId}/participants/{userId}")
    public ResponseEntity<BaseResponse<ChatRoomDto>> removeParticipant(
            @PathVariable String roomId,
            @PathVariable String userId) {
        try {
            ChatRoomDto updatedRoom = chatService.removeParticipant(roomId, userId);
            return ResponseEntity.ok(BaseResponse.success("Participant removed successfully", updatedRoom));
        } catch (Exception e) {
            log.error("Error removing participant: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/messages")
    public ResponseEntity<BaseResponse<ChatMessageDto>> sendMessage(@Valid @RequestBody ChatMessageDto messageDto) {
        try {
            ChatMessageDto sentMessage = chatService.sendMessage(messageDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success("Message sent successfully", sentMessage));
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<BaseResponse<Page<ChatMessageDto>>> getChatMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Page<ChatMessageDto> messages = chatService.getChatMessages(roomId, page, size);
        return ResponseEntity.ok(BaseResponse.success(messages));
    }

    @GetMapping("/rooms/{roomId}/messages/recent")
    public ResponseEntity<BaseResponse<List<ChatMessageDto>>> getRecentMessages(
            @PathVariable String roomId,
            @RequestParam String since) {

        LocalDateTime sinceTime = LocalDateTime.parse(since);
        List<ChatMessageDto> messages = chatService.getRecentMessages(roomId, sinceTime);
        return ResponseEntity.ok(BaseResponse.success(messages));
    }

    @GetMapping("/rooms/{roomId}/messages/search")
    public ResponseEntity<BaseResponse<List<ChatMessageDto>>> searchMessages(
            @PathVariable String roomId,
            @RequestParam String q) {

        List<ChatMessageDto> messages = chatService.searchMessages(roomId, q);
        return ResponseEntity.ok(BaseResponse.success(messages));
    }

    @PutMapping("/messages/{messageId}")
    public ResponseEntity<BaseResponse<ChatMessageDto>> editMessage(
            @PathVariable String messageId,
            @RequestBody String newContent) {
        try {
            ChatMessageDto editedMessage = chatService.editMessage(messageId, newContent);
            return ResponseEntity.ok(BaseResponse.success("Message edited successfully", editedMessage));
        } catch (Exception e) {
            log.error("Error editing message: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<BaseResponse<Void>> deleteMessage(@PathVariable String messageId) {
        try {
            chatService.deleteMessage(messageId);
            return ResponseEntity.ok(BaseResponse.success("Message deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting message: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/rooms/{roomId}/unread")
    public ResponseEntity<BaseResponse<Long>> getUnreadMessageCount(
            @PathVariable String roomId,
            @RequestParam String lastReadTime) {

        LocalDateTime lastRead = LocalDateTime.parse(lastReadTime);
        Long count = chatService.getUnreadMessageCount(roomId, lastRead);
        return ResponseEntity.ok(BaseResponse.success(count));
    }

    @GetMapping("/rooms/search")
    public ResponseEntity<BaseResponse<List<ChatRoomDto>>> searchChatRooms(
            @RequestParam String q,
            @RequestParam String userId) {

        List<ChatRoomDto> rooms = chatService.searchChatRooms(q, userId);
        return ResponseEntity.ok(BaseResponse.success(rooms));
    }

    @PostMapping("/rooms/project")
    public ResponseEntity<BaseResponse<ChatRoomDto>> createProjectChatRoom(
            @RequestParam String projectId,
            @RequestParam String projectName,
            @RequestParam String createdBy,
            @RequestBody Set<String> participants) {
        try {
            ChatRoomDto projectRoom = chatService.createProjectChatRoom(projectId, projectName, createdBy, participants);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success("Project chat room created successfully", projectRoom));
        } catch (Exception e) {
            log.error("Error creating project chat room: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage()));
        }
    }
}
