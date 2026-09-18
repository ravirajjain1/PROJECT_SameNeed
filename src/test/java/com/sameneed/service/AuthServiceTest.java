package com.sameneed.service;

import com.sameneed.exception.AuthException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    private final AuthService authService = new AuthService();

    @Test
    public void testLogin_blankEmail_throwsAuthException() {
        AuthException ex = assertThrows(AuthException.class,
                () -> authService.login("", "password123"));
        assertEquals(400, ex.getHttpStatus());
        assertEquals("Email is required", ex.getMessage());
    }

    @Test
    public void testLogin_blankPassword_throwsAuthException() {
        AuthException ex = assertThrows(AuthException.class,
                () -> authService.login("user@test.com", ""));
        assertEquals(400, ex.getHttpStatus());
        assertEquals("Password is required", ex.getMessage());
    }

    @Test
    public void testRegisterCustomer_shortPassword_throwsAuthException() {
        AuthException ex = assertThrows(AuthException.class,
                () -> authService.registerCustomer("a@b.com", "123", "Test User", null, "Pune"));
        assertEquals(400, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("6 characters"));
    }

    @Test
    public void testRegisterCustomer_invalidEmail_throwsAuthException() {
        AuthException ex = assertThrows(AuthException.class,
                () -> authService.registerCustomer("notanemail", "password123", "Test User", null, "Pune"));
        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    public void testRegisterCustomer_missingLocality_throwsAuthException() {
        AuthException ex = assertThrows(AuthException.class,
                () -> authService.registerCustomer("test@test.com", "password123", "Test User", null, ""));
        assertEquals(400, ex.getHttpStatus());
        assertEquals("Locality is required", ex.getMessage());
    }
}
