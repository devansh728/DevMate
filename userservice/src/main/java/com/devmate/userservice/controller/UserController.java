package com.devmate.userservice.controller;


import com.devmate.userservice.dto.BaseResponse;
import com.devmate.userservice.dto.UserDto;
import com.devmate.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<BaseResponse<UserDto>> createUser(@Valid @RequestBody UserDto userDto) {
        try {
            UserDto createdUser = userService.createUser(userDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.success("User created successfully", createdUser));
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<UserDto>> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(BaseResponse.success(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<BaseResponse<UserDto>> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(user -> ResponseEntity.ok(BaseResponse.success(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(BaseResponse.success(users));
    }

    @GetMapping("/available")
    public ResponseEntity<BaseResponse<List<UserDto>>> getAvailableUsers() {
        List<UserDto> users = userService.getAvailableUsers();
        return ResponseEntity.ok(BaseResponse.success(users));
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse<List<UserDto>>> searchUsers(@RequestParam String q) {
        List<UserDto> users = userService.searchUsers(q);
        return ResponseEntity.ok(BaseResponse.success(users));
    }

    @GetMapping("/skills")
    public ResponseEntity<BaseResponse<List<UserDto>>> getUsersBySkills(@RequestParam List<String> skills) {
        List<UserDto> users = userService.getUsersBySkills(skills);
        return ResponseEntity.ok(BaseResponse.success(users));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<UserDto>> updateUser(
            @PathVariable String id, 
            @Valid @RequestBody UserDto userDto) {
        try {
            UserDto updatedUser = userService.updateUser(id, userDto);
            return ResponseEntity.ok(BaseResponse.success("User updated successfully", updatedUser));
        } catch (Exception e) {
            log.error("Error updating user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/online-status")
    public ResponseEntity<BaseResponse<Void>> updateOnlineStatus(
            @PathVariable String id, 
            @RequestParam boolean isOnline) {
        try {
            userService.updateOnlineStatus(id, isOnline);
            return ResponseEntity.ok(BaseResponse.success("Online status updated successfully", null));
        } catch (Exception e) {
            log.error("Error updating online status: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteUser(@PathVariable String id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(BaseResponse.success("User deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error(e.getMessage()));
        }
    }
}
