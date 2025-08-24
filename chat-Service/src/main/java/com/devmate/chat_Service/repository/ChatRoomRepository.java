package com.devmate.chat_Service.repository;

import com.devmate.chat_Service.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    
    List<ChatRoom> findByIsActiveTrueOrderByUpdatedAtDesc();
    
    @Query("SELECT cr FROM ChatRoom cr WHERE :userId MEMBER OF cr.participants AND cr.isActive = true ORDER BY cr.updatedAt DESC")
    List<ChatRoom> findByUserIdAndIsActiveTrue(@Param("userId") String userId);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.type = :type AND cr.isActive = true ORDER BY cr.updatedAt DESC")
    List<ChatRoom> findByTypeAndIsActiveTrue(@Param("type") ChatRoom.ChatRoomType type);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.projectId = :projectId AND cr.isActive = true")
    Optional<ChatRoom> findByProjectIdAndIsActiveTrue(@Param("projectId") String projectId);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
           "cr.type = 'PRIVATE' AND " +
           "cr.participants = :participants AND " +
           "cr.isActive = true")
    Optional<ChatRoom> findPrivateRoomByParticipants(@Param("participants") java.util.Set<String> participants);
    
    @Query("SELECT cr FROM ChatRoom cr WHERE " +
           "LOWER(cr.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND " +
           "cr.isActive = true AND " +
           "(cr.type = 'PUBLIC' OR :userId MEMBER OF cr.participants)")
    List<ChatRoom> searchRooms(@Param("searchTerm") String searchTerm, @Param("userId") String userId);
    
    @Query("SELECT COUNT(cr) FROM ChatRoom cr WHERE :userId MEMBER OF cr.participants AND cr.isActive = true")
    Long countByUserIdAndIsActiveTrue(@Param("userId") String userId);
}
