package com.campus.system;

import com.campus.system.common.ApiResponse;
import com.campus.system.controller.AuthController;
import com.campus.system.controller.LostAndFoundController;
import com.campus.system.controller.UserController;
import com.campus.system.model.dto.LoginRequest;
import com.campus.system.model.dto.LostItemRequest;
import com.campus.system.model.dto.RegisterRequest;
import com.campus.system.model.entity.LostItem;
import com.campus.system.model.entity.User;
import com.campus.system.model.entity.UserProfile;
import com.campus.system.repository.InMemoryLostAndFoundRepository;
import com.campus.system.repository.InMemoryUserRepository;
import com.campus.system.service.impl.LostAndFoundServiceImpl;
import com.campus.system.service.impl.UserServiceImpl;
import java.util.List;

public class CampusSystemApplication {
    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("          CampusMate Smart Campus Life Service Platform (Sprint 1 & 2 Demo)     ");
        System.out.println("================================================================================");

        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        InMemoryLostAndFoundRepository lostRepo = new InMemoryLostAndFoundRepository();
        AuthController authController = new AuthController(new UserServiceImpl(userRepo));
        UserController userController = new UserController(new UserServiceImpl(userRepo));
        LostAndFoundController lostController = new LostAndFoundController(new LostAndFoundServiceImpl(lostRepo));

        System.out.println("\n--- [Step 1: Sprint 1 - Student Registration & JWT Authentication] ---");
        RegisterRequest regReq = new RegisterRequest("zhangsan", "Pass123456", "zhangsan@campus.edu", "Zhang San", "20230001", "Computer Science", "13800138000");
        ApiResponse<User> regResp = authController.register(regReq);
        System.out.println("1.1 Registration: " + regResp);

        ApiResponse<String> loginResp = authController.login(new LoginRequest("zhangsan", "Pass123456"));
        System.out.println("1.2 Login & Token: " + loginResp);

        ApiResponse<UserProfile> profileResp = userController.getProfile(1L);
        System.out.println("1.3 Query Profile: " + profileResp);

        System.out.println("\n--- [Step 2: Sprint 2 - Publish Lost Item (SUB-201~204)] ---");
        LostItemRequest itemReq = new LostItemRequest("Student ID Card (No.20230001)", "Blue lanyard, picked up on 3rd floor", "Library", "zhangsan");
        ApiResponse<LostItem> pubResp = lostController.publish(itemReq);
        System.out.println("2.1 Publish Item: " + pubResp);

        System.out.println("\n--- [Step 3: Sprint 2 - Keyword Fuzzy Search (SUB-202)] ---");
        ApiResponse<List<LostItem>> searchResp = lostController.search("Library");
        System.out.println("3.1 Search Keyword 'Library': Total found = " + searchResp.getData().size());
        searchResp.getData().forEach(item -> System.out.println("    -> " + item));

        System.out.println("\n--- [Step 4: Sprint 2 - Claim State Transition & Conflict Prevention (SUB-203)] ---");
        ApiResponse<LostItem> claimResp = lostController.claim(1L, "lisi");
        System.out.println("4.1 Normal Claim (by lisi): " + claimResp);

        ApiResponse<LostItem> dupClaimResp = lostController.claim(1L, "wangwu");
        System.out.println("4.2 Duplicate Claim Check (by wangwu): Code=" + dupClaimResp.getCode() + ", Msg='" + dupClaimResp.getMessage() + "'");

        System.out.println("\n================================================================================");
        System.out.println("                   Sprint 1 & Sprint 2 Live Demo Finished Successfully!         ");
        System.out.println("================================================================================");
    }
}