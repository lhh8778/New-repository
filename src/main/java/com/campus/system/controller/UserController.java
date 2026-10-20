package com.campus.system.controller;

import com.campus.system.common.ApiResponse;
import com.campus.system.model.entity.UserProfile;
import com.campus.system.service.UserService;

public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    public ApiResponse<UserProfile> getProfile(Long userId) {
        try {
            return ApiResponse.success(userService.getUserProfile(userId));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
}