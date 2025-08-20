package com.devmate.locationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String bio;
    private String avatarUrl;
    private List<String> skills;
    private List<String> interests;
    private String githubUsername;
    private String linkedinProfile;
    private boolean isOnline;
    private boolean isAvailableForCollaboration;
    private LocationDto location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
