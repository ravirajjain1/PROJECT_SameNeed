package com.sameneed.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OfferNegotiation {

    private int negotiationId;
    private int offerId;
    private String senderRole;
    private BigDecimal amount;
    private String message;
    private LocalDateTime createdAt;

    public OfferNegotiation() {}

    public int getNegotiationId() { return negotiationId; }
    public void setNegotiationId(int negotiationId) { this.negotiationId = negotiationId; }

    public int getOfferId() { return offerId; }
    public void setOfferId(int offerId) { this.offerId = offerId; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
