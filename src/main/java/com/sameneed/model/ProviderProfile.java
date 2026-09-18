package com.sameneed.model;

import jakarta.persistence.*;

@Entity
@Table(name = "service_providers")
public class ProviderProfile {

    @Id
    @Column(name = "provider_id")
    private int providerId;

    @Column(name = "business_name", length = 255)
    private String businessName;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "service_area", length = 255)
    private String serviceArea;

    @Column(name = "is_verified")
    private boolean verified;

    @Column(name = "avg_rating", precision = 3, scale = 2)
    private java.math.BigDecimal avgRating;

    public ProviderProfile() {}

    public int getProviderId() { return providerId; }
    public void setProviderId(int providerId) { this.providerId = providerId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getServiceArea() { return serviceArea; }
    public void setServiceArea(String serviceArea) { this.serviceArea = serviceArea; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public java.math.BigDecimal getAvgRating() { return avgRating; }
    public void setAvgRating(java.math.BigDecimal avgRating) { this.avgRating = avgRating; }
    public void setAvgRating(double avgRating) { this.avgRating = java.math.BigDecimal.valueOf(avgRating); }
}
