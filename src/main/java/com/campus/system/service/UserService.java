package com.campus.system.service;

import com.campus.system.model.dto.LoginRequest;
import com.campus.system.model.dto.RegisterRequest;
import com.campus.system.model.entity.User;
import com.campus.system.model.entity.UserProfile;

public interface UserService {
    User register(RegisterRequest request);
    String login(LoginRequest request);
    UserProfile getUserProfile(Long userId);
}