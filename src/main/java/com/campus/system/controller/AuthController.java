package com.campus.system.controller;

import com.campus.system.common.ApiResponse;
import com.campus.system.model.dto.LoginRequest;
import com.campus.system.model.dto.RegisterRequest;
import com.campus.system.model.entity.User;
import com.campus.system.service.UserService;

public class AuthController {
    private final UserService userService;
    public AuthController(UserService userService) { this.userService = userService; }

    public ApiResponse<User> register(RegisterRequest request) {
        try {
            return ApiResponse.success(userService.register(request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    public ApiResponse<String> login(LoginRequest request) {
        try {
            return ApiResponse.success(userService.login(request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(401, e.getMessage());
        }
    }
}