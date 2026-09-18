package com.sameneed.dao;

import com.sameneed.config.DatabaseConfig;
import com.sameneed.enums.UserRole;
import com.sameneed.exception.DatabaseException;
import com.sameneed.model.Admin;
import com.sameneed.model.BaseUser;
import com.sameneed.model.Customer;
import com.sameneed.model.ServiceProvider;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public int insertCustomer(Customer customer) {
        String userSql = "INSERT INTO users (email, password_hash, phone, role, display_name, locality) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseConfig.openConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, customer.getEmail());
                ps.setString(2, customer.getPasswordHash());
                ps.setString(3, customer.getPhone());
                ps.setString(4, UserRole.CUSTOMER.name());
                ps.setString(5, customer.getDisplayName());
                ps.setString(6, customer.getLocality());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    int id = keys.getInt(1);
                    conn.commit();
                    return id;
                }
                conn.rollback();
                return -1;
            } catch (SQLException e) {
                conn.rollback();
                throw new DatabaseException("Failed to insert customer", e);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Connection error inserting customer", e);
        }
    }

    public int insertProvider(ServiceProvider provider) {
        String userSql = "INSERT INTO users (email, password_hash, phone, role, display_name) VALUES (?,?,?,?,?)";
        String provSql = "INSERT INTO service_providers (provider_id, business_name, bio, service_area) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConfig.openConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, provider.getEmail());
                    ps.setString(2, provider.getPasswordHash());
                    ps.setString(3, provider.getPhone());
                    ps.setString(4, UserRole.SERVICE_PROVIDER.name());
                    ps.setString(5, provider.getDisplayName());
                    ps.executeUpdate();
                    ResultSet keys = ps.getGeneratedKeys();
                    if (!keys.next()) {
                        conn.rollback();
                        return -1;
                    }
                    userId = keys.getInt(1);
                }
                try (PreparedStatement ps2 = conn.prepareStatement(provSql)) {
                    ps2.setInt(1, userId);
                    ps2.setString(2, provider.getBusinessName());
                    ps2.setString(3, provider.getBio());
                    ps2.setString(4, provider.getServiceArea());
                    ps2.executeUpdate();
                }
                conn.commit();
                return userId;
            } catch (SQLException e) {
                conn.rollback();
                throw new DatabaseException("Failed to insert provider", e);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Connection error inserting provider", e);
        }
    }

    public BaseUser findByEmail(String email) {
        String sql = "SELECT u.*, sp.business_name, sp.bio, sp.service_area, sp.is_verified, sp.avg_rating " +
                     "FROM users u LEFT JOIN service_providers sp ON u.user_id = sp.provider_id " +
                     "WHERE u.email = ?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by email", e);
        }
        return null;
    }

    public BaseUser findById(int userId) {
        String sql = "SELECT u.*, sp.business_name, sp.bio, sp.service_area, sp.is_verified, sp.avg_rating " +
                     "FROM users u LEFT JOIN service_providers sp ON u.user_id = sp.provider_id " +
                     "WHERE u.user_id = ?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by id", e);
        }
        return null;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check email existence", e);
        }
    }

    public void updateProfile(int userId, String displayName, String phone, String locality) {
        String sql = "UPDATE users SET display_name=?, phone=?, locality=? WHERE user_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, displayName);
            ps.setString(2, phone);
            ps.setString(3, locality);
            ps.setInt(4, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user profile", e);
        }
    }

    public void updateProviderProfile(int providerId, String businessName, String bio, String serviceArea) {
        String sql = "UPDATE service_providers SET business_name=?, bio=?, service_area=? WHERE provider_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, businessName);
            ps.setString(2, bio);
            ps.setString(3, serviceArea);
            ps.setInt(4, providerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update provider profile", e);
        }
    }

    public void setActiveStatus(int userId, boolean active) {
        String sql = "UPDATE users SET is_active=? WHERE user_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user active status", e);
        }
    }

    public void setProviderVerified(int providerId, boolean verified) {
        String sql = "UPDATE service_providers SET is_verified=? WHERE provider_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, verified);
            ps.setInt(2, providerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update provider verification", e);
        }
    }

    public List<BaseUser> findAllByRole(UserRole role) {
        String sql = "SELECT u.*, sp.business_name, sp.bio, sp.service_area, sp.is_verified, sp.avg_rating " +
                     "FROM users u LEFT JOIN service_providers sp ON u.user_id = sp.provider_id " +
                     "WHERE u.role=? ORDER BY u.created_at DESC";
        List<BaseUser> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list users by role", e);
        }
        return list;
    }

    public void updateAvgRating(int providerId, double rating) {
        String sql = "UPDATE service_providers SET avg_rating=? WHERE provider_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, rating);
            ps.setInt(2, providerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update provider average rating", e);
        }
    }

    private BaseUser mapRow(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        int userId = rs.getInt("user_id");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String phone = rs.getString("phone");
        String displayName = rs.getString("display_name");
        boolean active = rs.getBoolean("is_active");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;

        if ("CUSTOMER".equals(role)) {
            return new Customer(userId, email, passwordHash, phone, displayName,
                    rs.getString("locality"), active, createdAt);
        } else if ("SERVICE_PROVIDER".equals(role)) {
            return new ServiceProvider(userId, email, passwordHash, phone, displayName,
                    rs.getString("business_name"), rs.getString("bio"),
                    rs.getString("service_area"), rs.getBoolean("is_verified"),
                    rs.getDouble("avg_rating"), active, createdAt);
        } else {
            return new Admin(userId, email, passwordHash, phone, displayName, active, createdAt);
        }
    }
}
