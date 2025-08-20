package com.devmate.locationservice.controller;

import com.devmate.locationservice.dto.LocationDto;
import com.devmate.locationservice.dto.BaseResponse;
import com.devmate.locationservice.dto.NearbyUsersResponseDto;
import com.devmate.locationservice.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<BaseResponse<LocationDto>> updateLocation(@Valid @RequestBody LocationDto locationDto) {
        try {
            LocationDto updatedLocation = locationService.updateUserLocation(locationDto);
            return ResponseEntity.ok(BaseResponse.success("Location updated successfully", updatedLocation));
        } catch (Exception e) {
            log.error("Error updating location: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<BaseResponse<LocationDto>> getUserLocation(@PathVariable String userId) {
        return locationService.getUserLocation(userId)
                .map(location -> ResponseEntity.ok(BaseResponse.success(location)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nearby")
    public ResponseEntity<BaseResponse<?>> getNearbyUsers(
            @RequestParam String userId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.error("User ID is required.", "INVALID_INPUT"));
        }
        if (latitude == null || longitude == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.error("Latitude and Longitude are required.", "INVALID_INPUT"));
        }
        if (radiusKm <= 0) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.error("Radius must be a positive value.", "INVALID_INPUT"));
        }

        try {
            NearbyUsersResponseDto nearbyUsers = locationService.getNearbyUsers(userId, latitude, longitude, radiusKm);
            return ResponseEntity
                    .ok(BaseResponse.success(nearbyUsers));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.error(e.getMessage(), "BUSINESS_RULE_VIOLATION"));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseResponse.error("An unexpected error occurred.", "UNEXPECTED_ERROR"));
        }
    }

    @GetMapping("/closest")
    public ResponseEntity<BaseResponse<List<LocationDto>>> getClosestUsers(
            @RequestParam String userId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") Integer limit) {
        
        List<LocationDto> closestUsers = locationService.getClosestUsers(userId, latitude, longitude, limit);
        return ResponseEntity.ok(BaseResponse.success(closestUsers));
    }

    @GetMapping("/nearby/count")
    public ResponseEntity<BaseResponse<Long>> countNearbyUsers(
            @RequestParam String userId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {
        
        Long count = locationService.countNearbyUsers(userId, latitude, longitude, radiusKm);
        return ResponseEntity.ok(BaseResponse.success(count));
    }

    @GetMapping("/active")
    public ResponseEntity<BaseResponse<List<LocationDto>>> getAllActiveLocations() {
        List<LocationDto> activeLocations = locationService.getAllActiveLocations();
        return ResponseEntity.ok(BaseResponse.success(activeLocations));
    }

    @GetMapping("/active/users")
    public ResponseEntity<BaseResponse<List<String>>> getActiveUserIds() {
        List<String> activeUserIds = locationService.getActiveUserIds();
        return ResponseEntity.ok(BaseResponse.success(activeUserIds));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<BaseResponse<Void>> deactivateUserLocation(@PathVariable String userId) {
        try {
            locationService.deactivateUserLocation(userId);
            return ResponseEntity.ok(BaseResponse.success("Location deactivated successfully", null));
        } catch (Exception e) {
            log.error("Error deactivating location: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/cleanup")
    public ResponseEntity<BaseResponse<Void>> cleanupOldLocations(
            @RequestParam(defaultValue = "24") Integer hoursOld) {
        try {
            locationService.cleanupOldLocations(hoursOld);
            return ResponseEntity.ok(BaseResponse.success("Cleanup initiated", null));
        } catch (Exception e) {
            log.error("Error during cleanup: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage()));
        }
    }
}
