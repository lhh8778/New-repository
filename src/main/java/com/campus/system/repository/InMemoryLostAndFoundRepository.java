package com.campus.system.repository;

import com.campus.system.model.entity.LostItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryLostAndFoundRepository implements LostAndFoundRepository {
    private final Map<Long, LostItem> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public LostItem save(LostItem item) {
        if (item.getId() == null) {
            item.setId(idGenerator.getAndIncrement());
        }
        storage.put(item.getId(), item);
        return item;
    }
    @Override
    public Optional<LostItem> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
    @Override
    public List<LostItem> findAll() {
        return new ArrayList<>(storage.values());
    }
    @Override
    public List<LostItem> findByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String lower = keyword.toLowerCase();
        return storage.values().stream()
                .filter(item -> item.getTitle().toLowerCase().contains(lower)
                        || item.getDescription().toLowerCase().contains(lower)
                        || item.getLocation().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }
}