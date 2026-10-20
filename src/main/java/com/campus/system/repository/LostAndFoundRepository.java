package com.campus.system.repository;

import com.campus.system.model.entity.LostItem;
import java.util.List;
import java.util.Optional;

public interface LostAndFoundRepository {
    LostItem save(LostItem item);
    Optional<LostItem> findById(Long id);
    List<LostItem> findAll();
    List<LostItem> findByKeyword(String keyword);
}