package com.devmate.userservice.mapper;


import com.devmate.userservice.dto.UserDto;
import com.devmate.userservice.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .skills(user.getSkills())
                .interests(user.getInterests())
                .githubUsername(user.getGithubUsername())
                .linkedinProfile(user.getLinkedinProfile())
                .isOnline(user.isOnline())
                .isAvailableForCollaboration(user.isAvailableForCollaboration())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .geoHash(user.getGeoHash())
                .build();
    }

    public User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        return User.builder()
                .id(userDto.getId())
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .fullName(userDto.getFullName())
                .bio(userDto.getBio())
                .avatarUrl(userDto.getAvatarUrl())
                .skills(userDto.getSkills())
                .interests(userDto.getInterests())
                .githubUsername(userDto.getGithubUsername())
                .linkedinProfile(userDto.getLinkedinProfile())
                .isOnline(userDto.isOnline())
                .geoHash(userDto.getGeoHash())
                .isAvailableForCollaboration(userDto.isAvailableForCollaboration())
                .build();
    }
}
