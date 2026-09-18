package com.sameneed.model;

import com.sameneed.enums.UserRole;
import java.time.LocalDateTime;

public abstract class BaseUser {

    private int userId;
    private String email;
    private String passwordHash;
    private String phone;
    private UserRole role;
    private String displayName;
    private boolean active;
    private LocalDateTime createdAt;

    protected BaseUser() {}

    protected BaseUser(int userId, String email, String passwordHash, String phone,
                       UserRole role, String displayName, boolean active, LocalDateTime createdAt) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.role = role;
        this.displayName = displayName;
        this.active = active;
        this.createdAt = createdAt;
    }

    public abstract String getSummary();

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
