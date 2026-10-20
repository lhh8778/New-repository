package com.campus.system.service.impl;

import com.campus.system.model.dto.LostItemRequest;
import com.campus.system.model.entity.ItemStatus;
import com.campus.system.model.entity.LostItem;
import com.campus.system.repository.LostAndFoundRepository;
import com.campus.system.service.LostAndFoundService;
import java.util.List;

public class LostAndFoundServiceImpl implements LostAndFoundService {
    private final LostAndFoundRepository repository;

    public LostAndFoundServiceImpl(LostAndFoundRepository repository) {
        this.repository = repository;
    }
    @Override
    public LostItem publishItem(LostItemRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Item title cannot be empty.");
        }
        LostItem item = new LostItem(null, request.getTitle(), request.getDescription(), request.getLocation(), request.getPublisherUsername());
        return repository.save(item);
    }
    @Override
    public List<LostItem> searchItems(String keyword) {
        return repository.findByKeyword(keyword);
    }
    @Override
    public LostItem claimItem(Long itemId, String claimantUsername) {
        LostItem item = repository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Lost item not found."));
        if (item.getStatus() == ItemStatus.CLAIMED) {
            throw new IllegalArgumentException("Item has already been claimed.");
        }
        item.setStatus(ItemStatus.CLAIMED);
        item.setClaimantUsername(claimantUsername);
        return repository.save(item);
    }
}