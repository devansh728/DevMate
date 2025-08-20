package com.devmate.userservice.repository;

import com.devmate.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    List<User> findByIsOnlineTrue();
    
    List<User> findByIsAvailableForCollaborationTrue();
    
    @Query("SELECT u FROM User u WHERE u.isOnline = true AND u.isAvailableForCollaboration = true")
    List<User> findAvailableOnlineUsers();
    
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "EXISTS (SELECT 1 FROM u.skills s WHERE LOWER(s) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT u FROM User u JOIN u.skills s WHERE LOWER(s) IN :skills")
    List<User> findBySkillsIn(@Param("skills") List<String> skills);
    
    @Modifying
    @Query("UPDATE User u SET u.isOnline = :isOnline, u.lastSeenAt = :lastSeenAt WHERE u.id = :userId")
    void updateUserOnlineStatus(@Param("userId") String userId, 
                               @Param("isOnline") boolean isOnline, 
                               @Param("lastSeenAt") LocalDateTime lastSeenAt);
}
