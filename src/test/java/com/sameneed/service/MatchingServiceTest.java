package com.sameneed.service;

import com.sameneed.model.ServiceRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MatchingServiceTest {

    private final MatchingService matchingService = new MatchingService();

    @Test
    public void testFindCompatibleRequests_emptyWithNullLocality() {
        List<ServiceRequest> result = matchingService.findCompatibleRequests(1, null, LocalDate.now());
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFindCompatibleRequests_emptyWithBlankLocality() {
        List<ServiceRequest> result = matchingService.findCompatibleRequests(1, "  ", LocalDate.now());
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFindCompatibleRequests_emptyWithZeroServiceId() {
        List<ServiceRequest> result = matchingService.findCompatibleRequests(0, "Koregaon Park", LocalDate.now());
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFindCompatibleRequests_emptyWithNullDate() {
        List<ServiceRequest> result = matchingService.findCompatibleRequests(1, "Koregaon Park", null);
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
