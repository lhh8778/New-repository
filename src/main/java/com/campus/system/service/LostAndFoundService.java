package com.campus.system.service;

import com.campus.system.model.dto.LostItemRequest;
import com.campus.system.model.entity.LostItem;
import java.util.List;

public interface LostAndFoundService {
    LostItem publishItem(LostItemRequest request);
    List<LostItem> searchItems(String keyword);
    LostItem claimItem(Long itemId, String claimantUsername);
}