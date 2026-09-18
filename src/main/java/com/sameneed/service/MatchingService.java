package com.sameneed.service;

import com.sameneed.dao.RequestDao;
import com.sameneed.model.ServiceRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MatchingService {

    private final RequestDao requestDao = new RequestDao();

    public List<ServiceRequest> findCompatibleRequests(int serviceId, String locality, LocalDate preferredDate) {
        if (serviceId <= 0 || locality == null || locality.isBlank() || preferredDate == null) {
            return new ArrayList<>();
        }
        List<ServiceRequest> candidates = requestDao.findCompatible(serviceId, locality.trim(), preferredDate);
        List<ServiceRequest> result = new ArrayList<>();
        for (ServiceRequest req : candidates) {
            if (isCompatible(req, serviceId, locality.trim(), preferredDate)) {
                result.add(req);
            }
        }
        return result;
    }

    private boolean isCompatible(ServiceRequest req, int serviceId, String locality, LocalDate date) {
        if (req.getServiceId() != serviceId) return false;
        if (!req.getPreferredDate().equals(date)) return false;
        if (!localityMatches(req.getLocality(), locality)) return false;
        if (!req.isOpen()) return false;
        if (!req.hasCapacity()) return false;
        return true;
    }

    private boolean localityMatches(String reqLocality, String searchLocality) {
        if (reqLocality == null || searchLocality == null) return false;
        String a = reqLocality.trim().toLowerCase();
        String b = searchLocality.trim().toLowerCase();
        return a.contains(b) || b.contains(a);
    }
}
