package com.devmate.locationservice.controller;

import com.devmate.locationservice.dto.LocationDto;
import com.devmate.locationservice.dto.NearbyUsersResponseDto;
import com.devmate.locationservice.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LocationWebSocketController {

    private final LocationService locationService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/location.update")
    public void updateLocation(@Payload LocationDto locationDto) {
        try {
            log.info("Received location update via WebSocket for user: {}", locationDto.getUserId());
            LocationDto updatedLocation = locationService.updateUserLocation(locationDto);
            NearbyUsersResponseDto nearbyUsers = locationService.getNearbyUsers(
                    locationDto.getUserId(),
                    locationDto.getLatitude(),
                    locationDto.getLongitude(),
                    5.0 // 5km radius
            );
            messagingTemplate.convertAndSendToUser(
                    locationDto.getUserId(),
                    "/queue/location.nearby",
                    nearbyUsers
            );
            nearbyUsers.getNearbyUsers().forEach(user -> {
                messagingTemplate.convertAndSendToUser(
                        user.getUserId(),
                        "/queue/location.updated",
                        updatedLocation
                );
            });

            log.info("Location update processed and notifications sent for user: {}", locationDto.getUserId());

        } catch (Exception e) {
            log.error("Error processing location update for user {}: {}", locationDto.getUserId(), e.getMessage());
            messagingTemplate.convertAndSendToUser(
                    locationDto.getUserId(),
                    "/queue/location.error",
                    "Failed to update location: " + e.getMessage()
            );
        }
    }

    @MessageMapping("/location.disconnect")
    public void disconnectUser(@Payload String userId) {
        try {
            log.info("User disconnecting: {}", userId);
            locationService.deactivateUserLocation(userId);
            messagingTemplate.convertAndSend(
                    "/topic/location.offline",
                    userId
            );

        } catch (Exception e) {
            log.error("Error processing user disconnect: {}", e.getMessage());
        }
    }
}