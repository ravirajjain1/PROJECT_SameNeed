package com.sameneed.model;

import com.sameneed.enums.UserRole;
import java.time.LocalDateTime;

public class ServiceProvider extends BaseUser {

    private String businessName;
    private String bio;
    private String serviceArea;
    private boolean verified;
    private double avgRating;

    public ServiceProvider() {
        super();
    }

    public ServiceProvider(int userId, String email, String passwordHash, String phone,
                           String displayName, String businessName, String bio, String serviceArea,
                           boolean verified, double avgRating, boolean active, LocalDateTime createdAt) {
        super(userId, email, passwordHash, phone, UserRole.SERVICE_PROVIDER, displayName, active, createdAt);
        this.businessName = businessName;
        this.bio = bio;
        this.serviceArea = serviceArea;
        this.verified = verified;
        this.avgRating = avgRating;
    }

    @Override
    public String getSummary() {
        return "Provider: " + businessName + " (Rating: " + avgRating + ")";
    }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getServiceArea() { return serviceArea; }
    public void setServiceArea(String serviceArea) { this.serviceArea = serviceArea; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public double getAvgRating() { return avgRating; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }
}
