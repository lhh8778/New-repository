package com.campus.system.model.entity;

public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private Role role;
    private UserProfile profile;

    public User() {}
    public User(Long id, String username, String password, String email, Role role, UserProfile profile) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.profile = profile;
    }
    public boolean validatePassword(String rawPassword) {
        return this.password != null && this.password.equals(rawPassword);
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public UserProfile getProfile() { return profile; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role + ", profile=" + profile + "}";
    }
}