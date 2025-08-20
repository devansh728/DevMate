package com.devmate.locationservice.repository;

import com.devmate.locationservice.entity.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, String> {
    
    Optional<UserLocation> findByUserIdAndIsActiveTrue(String userId);
    
    List<UserLocation> findByIsActiveTrueOrderByUpdatedAtDesc();

    @Query(value = """
    SELECT ul.*, COUNT(*) OVER() as total_nearby_users FROM user_locations ul 
    WHERE ul.is_active = true 
    AND ul.user_id != :userId
    AND ul.geohash IN :geohashesToSearch
    AND ST_DWithin(ul.location, ST_MakePoint(:longitude, :latitude)::geography, :radiusInMeters)
    ORDER BY ST_Distance(ul.location, ST_MakePoint(:longitude, :latitude)::geography)
    """, nativeQuery = true)
    List<UserLocation> findNearbyUsers(@Param("userId") String userId,
                                       @Param("geohashesToSearch") Set<String> geohashesToSearch,
                                       @Param("latitude") Double latitude,
                                       @Param("longitude") Double longitude,
                                       @Param("radiusInMeters") Double radiusInMeters);
    
    @Query(value = """
        SELECT ul.*, ST_Distance(ul.location, ST_MakePoint(:longitude, :latitude)::geography) as distance
        FROM user_locations ul 
        WHERE ul.is_active = true 
        AND ul.user_id != :userId
        ORDER BY distance
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findClosestUsers(@Param("userId") String userId,
                                   @Param("latitude") Double latitude,
                                   @Param("longitude") Double longitude,
                                   @Param("limit") Integer limit);
    
    @Query(value = """
        SELECT COUNT(*) FROM user_locations ul 
        WHERE ul.is_active = true 
        AND ul.user_id != :userId
        AND ST_DWithin(ul.location, ST_MakePoint(:longitude, :latitude)::geography, :radiusInMeters)
        """, nativeQuery = true)
    Long countNearbyUsers(@Param("userId") String userId,
                         @Param("latitude") Double latitude,
                         @Param("longitude") Double longitude,
                         @Param("radiusInMeters") Double radiusInMeters);
    
    @Modifying
    @Query("UPDATE UserLocation ul SET ul.isActive = false WHERE ul.userId = :userId AND ul.isActive = true")
    void deactivateUserLocations(@Param("userId") String userId);
    
    @Modifying
    @Query("DELETE FROM UserLocation ul WHERE ul.updatedAt < :cutoffTime")
    void deleteOldLocations(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT DISTINCT ul.userId FROM UserLocation ul WHERE ul.isActive = true")
    List<String> findActiveUserIds();
}
