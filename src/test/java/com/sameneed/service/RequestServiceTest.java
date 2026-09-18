package com.sameneed.service;

import com.sameneed.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RequestServiceTest {

    private final RequestService requestService = new RequestService();

    @Test
    public void testCreateRequest_noServiceId_throwsInvalidRequestException() {
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest(1, 0, "Problem", "Locality", "2027-01-01", "10:00", null, 5));
        assertEquals("Service must be selected", ex.getMessage());
    }

    @Test
    public void testCreateRequest_blankProblem_throwsInvalidRequestException() {
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest(1, 1, "", "Locality", "2027-01-01", "10:00", null, 5));
        assertEquals("Problem description is required", ex.getMessage());
    }

    @Test
    public void testCreateRequest_groupSizeTooLarge_throwsInvalidRequestException() {
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest(1, 1, "My problem", "Locality", "2027-01-01", "10:00", null, 15));
        assertTrue(ex.getMessage().contains("10"));
    }

    @Test
    public void testCreateRequest_groupSizeTooSmall_throwsInvalidRequestException() {
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest(1, 1, "My problem", "Locality", "2027-01-01", "10:00", null, 1));
        assertTrue(ex.getMessage().contains("2"));
    }

    @Test
    public void testCreateRequest_invalidDateFormat_throwsInvalidRequestException() {
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest(1, 1, "My problem", "Locality", "not-a-date", "10:00", null, 5));
        assertEquals("Invalid preferred date", ex.getMessage());
    }

    @Test
    public void testCreateRequest_negativeBudget_throwsInvalidRequestException() {
        InvalidRequestException ex = assertThrows(InvalidRequestException.class,
                () -> requestService.createRequest(1, 1, "My problem", "Locality", "2027-01-01", "10:00", "-100", 5));
        assertEquals("Budget cannot be negative", ex.getMessage());
    }
}
