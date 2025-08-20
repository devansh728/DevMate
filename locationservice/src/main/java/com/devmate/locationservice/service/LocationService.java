package com.devmate.locationservice.service;

import com.devmate.locationservice.dto.LocationDto;
import com.devmate.locationservice.dto.NearbyUsersResponseDto;
import com.devmate.locationservice.entity.UserLocation;
import com.devmate.locationservice.repository.UserLocationRepository;
import com.devmate.locationservice.utils.GeoHash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LocationService {

    private final UserLocationRepository locationRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public LocationDto updateUserLocation(LocationDto locationDto) {
        log.info("Updating location for user: {}", locationDto.getUserId());
        
        // Deactivate previous locations for this user
        locationRepository.deactivateUserLocations(locationDto.getUserId());

        String geoHash = GeoHash.encode(locationDto.getLatitude(), locationDto.getLongitude(), 8);
        locationDto.setGeoHash(geoHash);

        // Create new location entry
        Point point = createPoint(locationDto.getLongitude(), locationDto.getLatitude());
        
        UserLocation userLocation = UserLocation.builder()
                .userId(locationDto.getUserId())
                .location(point)
                .latitude(locationDto.getLatitude())
                .longitude(locationDto.getLongitude())
                .address(locationDto.getAddress())
                .accuracy(locationDto.getAccuracy())
                .geoHash(locationDto.getGeoHash())
                .isActive(true)
                .build();
        
        UserLocation savedLocation = locationRepository.save(userLocation);
        log.info("Location updated successfully for user: {}", locationDto.getUserId());
        
        return mapToDto(savedLocation);
    }

    public Optional<LocationDto> getUserLocation(String userId) {
        return locationRepository.findByUserIdAndIsActiveTrue(userId)
                .map(this::mapToDto);
    }

    public NearbyUsersResponseDto getNearbyUsers(String userId, Double latitude, Double longitude, Double radiusKm) {
        double radiusInMeters = radiusKm * 1000; // Convert km to meters
        int precision = getGeohashPrecision(radiusInMeters);
        String centerGeohash = GeoHash.encode(latitude, longitude, precision);

        Set<String> geohashesToSearch = new HashSet<>();
        geohashesToSearch.add(centerGeohash);
        geohashesToSearch.addAll(Arrays.asList(GeoHash.getAdjacentHashes(centerGeohash)));

        List<UserLocation> nearbyLocations = locationRepository.findNearbyUsers(
                userId, geohashesToSearch, latitude, longitude, radiusInMeters);

        List<LocationDto> locationDtos =  nearbyLocations.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        long totalCount = locationDtos.size();

        return new NearbyUsersResponseDto(locationDtos, totalCount);
    }

    public List<LocationDto> getClosestUsers(String userId, Double latitude, Double longitude, Integer limit) {
        List<Object[]> results = locationRepository.findClosestUsers(userId, latitude, longitude, limit);
        
        return results.stream()
                .map(result -> {
                    UserLocation location = (UserLocation) result[0];
                    // Distance is in result[1] if needed
                    return mapToDto(location);
                })
                .collect(Collectors.toList());
    }

    public Long countNearbyUsers(String userId, Double latitude, Double longitude, Double radiusKm) {
        Double radiusInMeters = radiusKm * 1000;
        return locationRepository.countNearbyUsers(userId, latitude, longitude, radiusInMeters);
    }

    public List<LocationDto> getAllActiveLocations() {
        return locationRepository.findByIsActiveTrueOrderByUpdatedAtDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deactivateUserLocation(String userId) {
        locationRepository.deactivateUserLocations(userId);
        log.info("Deactivated location for user: {}", userId);
    }

    @Transactional
    @Async
    public CompletableFuture<Void> cleanupOldLocations(int hoursOld) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursOld);
        locationRepository.deleteOldLocations(cutoffTime);
        log.info("Cleaned up locations older than {} hours", hoursOld);
        return CompletableFuture.completedFuture(null);
    }

    public List<String> getActiveUserIds() {
        return locationRepository.findActiveUserIds();
    }

    private Point createPoint(Double longitude, Double latitude) {
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }

    private LocationDto mapToDto(UserLocation userLocation) {
        return LocationDto.builder()
                .userId(userLocation.getUserId())
                .latitude(userLocation.getLatitude())
                .longitude(userLocation.getLongitude())
                .address(userLocation.getAddress())
                .accuracy(userLocation.getAccuracy())
                .build();
    }

    private Set<String> getRelevantGeohashPrefixes(String centerGeohash, double radius) {
        Set<String> prefixes = new HashSet<>();
        prefixes.add(centerGeohash.substring(0, centerGeohash.length() - 2));

        if (radius > 1000) { // Only needed for larger radii
            Arrays.stream(GeoHash.getAdjacentHashes(centerGeohash))
                    .forEach(adj -> prefixes.add(adj.substring(0, adj.length() - 2)));
        }
        return prefixes;
    }

    private int getGeohashPrecision(double radiusMeters) {
        if (radiusMeters < 100) return 8;  // ~19m precision
        if (radiusMeters < 500) return 7;  // ~76m
        if (radiusMeters < 2000) return 6; // ~610m
        if (radiusMeters < 10000) return 5; // ~2.4km
        return 4; // ~20km
    }
}
