package com.sameneed.model;

import com.sameneed.enums.OfferStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Offer {

    private int offerId;
    private int requestId;
    private int providerId;
    private String providerName;
    private BigDecimal amountPerMember;
    private int requiredMembers;
    private LocalDateTime validUntil;
    private OfferStatus status;
    private LocalDateTime createdAt;

    public Offer() {}

    public int getOfferId() { return offerId; }
    public void setOfferId(int offerId) { this.offerId = offerId; }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getProviderId() { return providerId; }
    public void setProviderId(int providerId) { this.providerId = providerId; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public BigDecimal getAmountPerMember() { return amountPerMember; }
    public void setAmountPerMember(BigDecimal amountPerMember) { this.amountPerMember = amountPerMember; }

    public int getRequiredMembers() { return requiredMembers; }
    public void setRequiredMembers(int requiredMembers) { this.requiredMembers = requiredMembers; }

    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }

    public OfferStatus getStatus() { return status; }
    public void setStatus(OfferStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(validUntil);
    }
}
