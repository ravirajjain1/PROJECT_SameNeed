package com.sameneed.model;

import com.sameneed.enums.UserRole;
import java.time.LocalDateTime;

public class Customer extends BaseUser {

    private String locality;

    public Customer() {
        super();
    }

    public Customer(int userId, String email, String passwordHash, String phone,
                    String displayName, String locality, boolean active, LocalDateTime createdAt) {
        super(userId, email, passwordHash, phone, UserRole.CUSTOMER, displayName, active, createdAt);
        this.locality = locality;
    }

    @Override
    public String getSummary() {
        return "Customer: " + getDisplayName() + " (" + locality + ")";
    }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }
}
