package com.devmate.userservice.service;

import com.devmate.userservice.dto.UserDto;
import com.devmate.userservice.entity.User;
import com.devmate.userservice.mapper.UserMapper;
import com.devmate.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Creating user with username: {}", userDto.getUsername());
        
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);
        
        log.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    public Optional<UserDto> getUserById(String id) {
        return userRepository.findById(id)
                .map(userMapper::toDto);
    }

    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDto);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getAvailableUsers() {
        return userRepository.findAvailableOnlineUsers()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> searchUsers(String searchTerm) {
        return userRepository.searchUsers(searchTerm)
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getUsersBySkills(List<String> skills) {
        List<String> lowerCaseSkills = skills.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        
        return userRepository.findBySkillsIn(lowerCaseSkills)
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto updateUser(String id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update fields
        existingUser.setFullName(userDto.getFullName());
        existingUser.setBio(userDto.getBio());
        existingUser.setAvatarUrl(userDto.getAvatarUrl());
        existingUser.setSkills(userDto.getSkills());
        existingUser.setInterests(userDto.getInterests());
        existingUser.setGithubUsername(userDto.getGithubUsername());
        existingUser.setLinkedinProfile(userDto.getLinkedinProfile());
        existingUser.setAvailableForCollaboration(userDto.isAvailableForCollaboration());

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public void updateOnlineStatus(String userId, boolean isOnline) {
        userRepository.updateUserOnlineStatus(userId, isOnline, LocalDateTime.now());
        log.info("Updated online status for user {}: {}", userId, isOnline);
    }

    @Transactional
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
        log.info("User deleted: {}", id);
    }
}
