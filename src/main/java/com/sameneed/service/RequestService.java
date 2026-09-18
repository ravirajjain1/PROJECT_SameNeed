package com.sameneed.service;

import com.sameneed.dao.RequestDao;
import com.sameneed.dao.RequestMemberDao;
import com.sameneed.enums.RequestStatus;
import com.sameneed.exception.InvalidRequestException;
import com.sameneed.model.RequestMember;
import com.sameneed.model.ServiceRequest;
import com.sameneed.util.AliasGenerator;
import com.sameneed.util.DateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class RequestService {

    private final RequestDao requestDao = new RequestDao();
    private final RequestMemberDao memberDao = new RequestMemberDao();

    public ServiceRequest createRequest(int creatorId, int serviceId, String problem,
                                        String locality, String dateStr, String timeStr,
                                        String budgetStr, int maxGroupSize) {
        if (serviceId <= 0) throw new InvalidRequestException("Service must be selected");
        if (problem == null || problem.isBlank()) throw new InvalidRequestException("Problem description is required");
        if (locality == null || locality.isBlank()) throw new InvalidRequestException("Locality is required");

        LocalDate date = DateUtil.parseDate(dateStr);
        if (date == null) throw new InvalidRequestException("Invalid preferred date");
        if (date.isBefore(LocalDate.now())) throw new InvalidRequestException("Preferred date cannot be in the past");

        LocalTime time = DateUtil.parseTime(timeStr);
        if (time == null) throw new InvalidRequestException("Invalid preferred time");

        if (maxGroupSize < 2 || maxGroupSize > 10) {
            throw new InvalidRequestException("Group size must be between 2 and 10");
        }

        BigDecimal budget = null;
        if (budgetStr != null && !budgetStr.isBlank()) {
            try {
                budget = new BigDecimal(budgetStr.trim());
                if (budget.compareTo(BigDecimal.ZERO) < 0) {
                    throw new InvalidRequestException("Budget cannot be negative");
                }
            } catch (NumberFormatException e) {
                throw new InvalidRequestException("Invalid budget format");
            }
        }

        ServiceRequest req = new ServiceRequest();
        req.setCreatorId(creatorId);
        req.setServiceId(serviceId);
        req.setProblemDescription(problem.trim());
        req.setLocality(locality.trim());
        req.setPreferredDate(date);
        req.setPreferredTime(time);
        req.setBudgetExpectation(budget);
        req.setMaxGroupSize(maxGroupSize);
        req.setStatus(RequestStatus.REQUESTED);

        int requestId = requestDao.insert(req);
        req.setRequestId(requestId);

        RequestMember creatorMembership = new RequestMember(requestId, creatorId, AliasGenerator.generate(1));
        memberDao.insert(creatorMembership);

        return req;
    }

    public ServiceRequest getById(int requestId) {
        ServiceRequest req = requestDao.findById(requestId);
        if (req == null) throw new InvalidRequestException("Request not found");
        return req;
    }

    public List<ServiceRequest> getByCreator(int creatorId) {
        return requestDao.findByCreator(creatorId);
    }

    public List<ServiceRequest> getByMember(int userId) {
        return requestDao.findByMember(userId);
    }

    public List<ServiceRequest> getOpenRequests() {
        return requestDao.findAllOpen();
    }

    public List<ServiceRequest> getEligibleForProvider(int providerId) {
        return requestDao.findEligibleForProvider(providerId);
    }

    public void updateStatus(int requestId, RequestStatus newStatus, int requestingUserId) {
        ServiceRequest req = getById(requestId);
        if (req.getCreatorId() != requestingUserId) {
            throw new InvalidRequestException("Only the group creator can update the request status");
        }
        requestDao.updateStatus(requestId, newStatus);
    }

    public List<ServiceRequest> getAll(int limit, int offset) {
        return requestDao.findAll(limit, offset);
    }

    public int countAll() {
        return requestDao.countAll();
    }
}
