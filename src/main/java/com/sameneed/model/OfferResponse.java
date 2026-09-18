package com.sameneed.model;

import com.sameneed.enums.MemberResponseStatus;
import java.time.LocalDateTime;

public class OfferResponse {

    private int responseId;
    private int offerId;
    private int memberId;
    private String memberAlias;
    private MemberResponseStatus status;
    private LocalDateTime respondedAt;

    public OfferResponse() {}

    public int getResponseId() { return responseId; }
    public void setResponseId(int responseId) { this.responseId = responseId; }

    public int getOfferId() { return offerId; }
    public void setOfferId(int offerId) { this.offerId = offerId; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getMemberAlias() { return memberAlias; }
    public void setMemberAlias(String memberAlias) { this.memberAlias = memberAlias; }

    public MemberResponseStatus getStatus() { return status; }
    public void setStatus(MemberResponseStatus status) { this.status = status; }

    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
}
