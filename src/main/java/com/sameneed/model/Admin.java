package com.sameneed.model;

import com.sameneed.enums.UserRole;
import java.time.LocalDateTime;

public class Admin extends BaseUser {

    public Admin() {
        super();
    }

    public Admin(int userId, String email, String passwordHash, String phone,
                 String displayName, boolean active, LocalDateTime createdAt) {
        super(userId, email, passwordHash, phone, UserRole.ADMIN, displayName, active, createdAt);
    }

    @Override
    public String getSummary() {
        return "Admin: " + getDisplayName();
    }
}
