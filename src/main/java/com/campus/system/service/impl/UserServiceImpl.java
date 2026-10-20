package com.campus.system.service.impl;

import com.campus.system.model.dto.LoginRequest;
import com.campus.system.model.dto.RegisterRequest;
import com.campus.system.model.entity.Role;
import com.campus.system.model.entity.User;
import com.campus.system.model.entity.UserProfile;
import com.campus.system.repository.UserRepository;
import com.campus.system.service.UserService;
import java.util.UUID;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        UserProfile profile = new UserProfile(null, request.getRealName(), request.getStudentId(), request.getDepartment(), request.getPhone());
        User user = new User(null, request.getUsername(), request.getPassword(), request.getEmail(), Role.STUDENT, profile);
        return userRepository.save(user);
    }
    @Override
    public String login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));
        if (!user.validatePassword(request.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        return "JWT_TOKEN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16) + "_" + user.getId();
    }
    @Override
    public UserProfile getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."))
                .getProfile();
    }
}