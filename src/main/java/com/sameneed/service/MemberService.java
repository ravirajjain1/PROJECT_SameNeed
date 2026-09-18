package com.sameneed.service;

import com.sameneed.dao.RequestDao;
import com.sameneed.dao.RequestMemberDao;
import com.sameneed.enums.RequestStatus;
import com.sameneed.exception.InvalidRequestException;
import com.sameneed.model.RequestMember;
import com.sameneed.model.ServiceRequest;
import com.sameneed.util.AliasGenerator;

import java.util.List;

public class MemberService {

    private final RequestDao requestDao = new RequestDao();
    private final RequestMemberDao memberDao = new RequestMemberDao();

    public RequestMember joinRequest(int requestId, int userId) {
        ServiceRequest req = requestDao.findById(requestId);
        if (req == null) throw new InvalidRequestException("Request not found");
        if (!req.isOpen()) throw new InvalidRequestException("This request is no longer accepting members");
        if (!req.hasCapacity()) throw new InvalidRequestException("This group is full");
        if (req.getCreatorId() == userId) throw new InvalidRequestException("You created this request and are already a member");
        if (memberDao.isMember(requestId, userId)) throw new InvalidRequestException("You have already joined this request");

        int currentCount = memberDao.countActiveMembers(requestId);
        String alias = AliasGenerator.generateForCount(currentCount);

        RequestMember member = new RequestMember(requestId, userId, alias);
        int memberId = memberDao.insert(member);
        member.setMemberId(memberId);

        requestDao.incrementMemberCount(requestId);

        int newCount = currentCount + 1;
        if (newCount >= 2 && req.getStatus() == RequestStatus.REQUESTED) {
            requestDao.updateStatus(requestId, RequestStatus.GROUP_FORMING);
        }

        return member;
    }

    public void leaveRequest(int requestId, int userId) {
        ServiceRequest req = requestDao.findById(requestId);
        if (req == null) throw new InvalidRequestException("Request not found");
        if (req.getCreatorId() == userId) {
            throw new InvalidRequestException("The group creator cannot leave. You may close the request instead.");
        }
        RequestMember member = memberDao.findByRequestAndUser(requestId, userId);
        if (member == null) throw new InvalidRequestException("You are not a member of this request");

        memberDao.deactivate(member.getMemberId());
        requestDao.decrementMemberCount(requestId);
    }

    public List<RequestMember> getMembers(int requestId) {
        return memberDao.findByRequest(requestId);
    }

    public boolean isMember(int requestId, int userId) {
        return memberDao.isMember(requestId, userId);
    }

    public RequestMember getMemberRecord(int requestId, int userId) {
        return memberDao.findByRequestAndUser(requestId, userId);
    }
}
