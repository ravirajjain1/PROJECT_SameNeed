package com.sameneed.service;

import com.sameneed.dao.UserDao;
import com.sameneed.enums.UserRole;
import com.sameneed.exception.AuthException;
import com.sameneed.model.BaseUser;
import com.sameneed.model.Customer;
import com.sameneed.model.ServiceProvider;
import com.sameneed.util.PasswordUtil;

public class AuthService {

    private final UserDao userDao = new UserDao();

    public BaseUser login(String email, String password) {
        if (email == null || email.isBlank()) throw new AuthException("Email is required", 400);
        if (password == null || password.isBlank()) throw new AuthException("Password is required", 400);

        BaseUser user = userDao.findByEmail(email.trim().toLowerCase());
        if (user == null) throw new AuthException("Invalid email or password", 401);
        if (!user.isActive()) throw new AuthException("Your account has been suspended", 403);
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new AuthException("Invalid email or password", 401);
        }
        return user;
    }

    public Customer registerCustomer(String email, String password, String displayName, String phone, String locality) {
        validateRegistration(email, password, displayName);
        if (locality == null || locality.isBlank()) throw new AuthException("Locality is required", 400);

        Customer customer = new Customer();
        customer.setEmail(email.trim().toLowerCase());
        customer.setPasswordHash(PasswordUtil.hash(password));
        customer.setDisplayName(displayName.trim());
        customer.setPhone(phone != null ? phone.trim() : null);
        customer.setLocality(locality.trim());
        customer.setRole(UserRole.CUSTOMER);

        int id = userDao.insertCustomer(customer);
        if (id < 0) throw new AuthException("Registration failed", 500);
        customer.setUserId(id);
        return customer;
    }

    public ServiceProvider registerProvider(String email, String password, String displayName,
                                            String phone, String businessName, String bio, String serviceArea) {
        validateRegistration(email, password, displayName);
        if (businessName == null || businessName.isBlank()) {
            throw new AuthException("Business name is required", 400);
        }

        ServiceProvider provider = new ServiceProvider();
        provider.setEmail(email.trim().toLowerCase());
        provider.setPasswordHash(PasswordUtil.hash(password));
        provider.setDisplayName(displayName.trim());
        provider.setPhone(phone != null ? phone.trim() : null);
        provider.setBusinessName(businessName.trim());
        provider.setBio(bio != null ? bio.trim() : null);
        provider.setServiceArea(serviceArea != null ? serviceArea.trim() : null);
        provider.setRole(UserRole.SERVICE_PROVIDER);

        int id = userDao.insertProvider(provider);
        if (id < 0) throw new AuthException("Registration failed", 500);
        provider.setUserId(id);
        return provider;
    }

    private void validateRegistration(String email, String password, String displayName) {
        if (email == null || email.isBlank()) throw new AuthException("Email is required", 400);
        if (!email.contains("@")) throw new AuthException("Invalid email format", 400);
        if (password == null || password.length() < 6) {
            throw new AuthException("Password must be at least 6 characters", 400);
        }
        if (displayName == null || displayName.isBlank()) {
            throw new AuthException("Display name is required", 400);
        }
        if (userDao.emailExists(email.trim().toLowerCase())) {
            throw new AuthException("An account with this email already exists", 409);
        }
    }
}
