package com.devmate.locationservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class NearbyUsersResponseDto {
    private final List<LocationDto> nearbyUsers;
    private final long totalCount;
}
