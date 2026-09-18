package com.sameneed.model;

import java.time.LocalDateTime;

public class RequestMember {

    private int memberId;
    private int requestId;
    private int userId;
    private String anonymousAlias;
    private boolean active;
    private LocalDateTime joinedAt;

    public RequestMember() {}

    public RequestMember(int requestId, int userId, String anonymousAlias) {
        this.requestId = requestId;
        this.userId = userId;
        this.anonymousAlias = anonymousAlias;
        this.active = true;
    }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAnonymousAlias() { return anonymousAlias; }
    public void setAnonymousAlias(String anonymousAlias) { this.anonymousAlias = anonymousAlias; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
