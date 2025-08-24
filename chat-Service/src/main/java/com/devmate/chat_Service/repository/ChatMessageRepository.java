package com.devmate.chat_Service.repository;

import com.devmate.chat_Service.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    
    Page<ChatMessage> findByChatRoomIdAndIsDeletedFalseOrderByTimestampDesc(String chatRoomId, Pageable pageable);
    
    List<ChatMessage> findByChatRoomIdAndIsDeletedFalseOrderByTimestampAsc(String chatRoomId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE " +
           "cm.chatRoomId = :chatRoomId AND " +
           "cm.isDeleted = false AND " +
           "cm.timestamp >= :since " +
           "ORDER BY cm.timestamp ASC")
    List<ChatMessage> findRecentMessages(@Param("chatRoomId") String chatRoomId, 
                                       @Param("since") LocalDateTime since);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE " +
           "cm.chatRoomId = :chatRoomId AND " +
           "cm.isDeleted = false AND " +
           "LOWER(cm.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY cm.timestamp DESC")
    List<ChatMessage> searchMessagesInRoom(@Param("chatRoomId") String chatRoomId, 
                                         @Param("searchTerm") String searchTerm);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE " +
           "cm.chatRoomId = :chatRoomId AND " +
           "cm.isDeleted = false " +
           "ORDER BY cm.timestamp DESC " +
           "LIMIT 1")
    Optional<ChatMessage> findLastMessage(@Param("chatRoomId") String chatRoomId);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE " +
           "cm.chatRoomId = :chatRoomId AND " +
           "cm.isDeleted = false AND " +
           "cm.timestamp > :lastReadTime")
    Long countUnreadMessages(@Param("chatRoomId") String chatRoomId, 
                           @Param("lastReadTime") LocalDateTime lastReadTime);
    
    List<ChatMessage> findBySenderIdAndIsDeletedFalseOrderByTimestampDesc(String senderId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE " +
           "cm.replyToMessageId = :messageId AND " +
           "cm.isDeleted = false " +
           "ORDER BY cm.timestamp ASC")
    List<ChatMessage> findReplies(@Param("messageId") String messageId);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE " +
           "cm.chatRoomId = :chatRoomId AND " +
           "cm.senderId = :senderId AND " +
           "cm.timestamp >= :since")
    Long countMessagesByUserSince(@Param("chatRoomId") String chatRoomId, 
                                 @Param("senderId") String senderId, 
                                 @Param("since") LocalDateTime since);
}
