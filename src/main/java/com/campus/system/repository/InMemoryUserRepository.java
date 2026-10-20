package com.campus.system.repository;

import com.campus.system.model.entity.User;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Optional<User> findByUsername(String username) {
        return storage.values().stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }
    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
    @Override
    public boolean existsByUsername(String username) {
        return storage.values().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }
    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        if (user.getProfile() != null && user.getProfile().getId() == null) {
            user.getProfile().setId(user.getId());
        }
        storage.put(user.getId(), user);
        return user;
    }
}