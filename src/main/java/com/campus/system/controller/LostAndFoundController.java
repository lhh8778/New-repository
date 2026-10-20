package com.campus.system.controller;

import com.campus.system.common.ApiResponse;
import com.campus.system.model.dto.LostItemRequest;
import com.campus.system.model.entity.LostItem;
import com.campus.system.service.LostAndFoundService;
import java.util.List;

public class LostAndFoundController {
    private final LostAndFoundService service;
    public LostAndFoundController(LostAndFoundService service) { this.service = service; }

    public ApiResponse<LostItem> publish(LostItemRequest request) {
        try {
            return ApiResponse.success(service.publishItem(request));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    public ApiResponse<List<LostItem>> search(String keyword) {
        return ApiResponse.success(service.searchItems(keyword));
    }
    public ApiResponse<LostItem> claim(Long itemId, String claimantUsername) {
        try {
            return ApiResponse.success(service.claimItem(itemId, claimantUsername));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}